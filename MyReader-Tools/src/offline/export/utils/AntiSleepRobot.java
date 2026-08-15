package offline.export.utils;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import offline.export.IConstants;

/**
 * 禁止锁屏禁止休眠
 * 
 * @author 刘绕兴
 */
public class AntiSleepRobot {
	
	private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	
	private static Robot robot;

	public static int MOUSEMOVE_SHAKE_RANGE = 10;

	public static void switchAntiSleep() {
		boolean isAntiSleep = "true".equals(System.getProperty(IConstants.PROP_ANTI_SLEEP, "false"));
		if (isAntiSleep)
			AntiSleepRobot.stopPreventingSleep();
		else
			AntiSleepRobot.startPreventingSleep();
		System.setProperty(IConstants.PROP_ANTI_SLEEP, String.valueOf(!isAntiSleep));
	}
	
	public static void startPreventingSleep() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			System.err.println("无法创建 Robot 实例: " + e.getMessage());
			return;
		}
		scheduler.scheduleAtFixedRate(() -> {
			try {
				java.awt.Point currentPos = java.awt.MouseInfo.getPointerInfo().getLocation();

				robot.mouseMove(currentPos.x + MOUSEMOVE_SHAKE_RANGE, currentPos.y + MOUSEMOVE_SHAKE_RANGE);
				Thread.sleep(10);
				currentPos = java.awt.MouseInfo.getPointerInfo().getLocation();
				robot.mouseMove(currentPos.x - MOUSEMOVE_SHAKE_RANGE, currentPos.y - MOUSEMOVE_SHAKE_RANGE);
				
				robot.keyPress(KeyEvent.VK_SHIFT);
				robot.keyRelease(KeyEvent.VK_SHIFT);
			} catch (Exception e) {
				System.err.println("发送活动信号失败: " + e.getMessage());
			}
		}, 0, 30, TimeUnit.SECONDS); // 每30秒一次
	}

	public static void stopPreventingSleep() {
		scheduler.shutdown();
		try {
			if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
				scheduler.shutdownNow();
			}
		} catch (InterruptedException e) {
			scheduler.shutdownNow();
		}
	}
	
	public static void main(String[] args) {
		AntiSleepRobot.startPreventingSleep();
	}
}