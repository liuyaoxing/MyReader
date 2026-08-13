package offline.export.utils;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AntiSleepRobot {
	private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private static Robot robot;

	public static void startPreventingSleep() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			System.err.println("无法创建 Robot 实例: " + e.getMessage());
			return;
		}
		AtomicBoolean ab = new AtomicBoolean(false);
		int SHAKE_RANGE = 10;
		// 每 60 秒执行一次，重置系统空闲计时器
		scheduler.scheduleAtFixedRate(() -> {
			try {
				// 获取当前鼠标位置
				java.awt.Point currentPos = java.awt.MouseInfo.getPointerInfo().getLocation();

				robot.mouseMove(currentPos.x + SHAKE_RANGE, currentPos.y + SHAKE_RANGE);
				Thread.sleep(10);
				currentPos = java.awt.MouseInfo.getPointerInfo().getLocation();
				robot.mouseMove(currentPos.x - SHAKE_RANGE, currentPos.y - SHAKE_RANGE);
				
				// 可选：模拟一个极短的按键事件，增加“活动”权重
				robot.keyPress(KeyEvent.VK_SHIFT);
				robot.keyRelease(KeyEvent.VK_SHIFT);
				
				ab.set(!ab.get());
				System.out.println("Sent activity signal to prevent sleep.");
			} catch (Exception e) {
				System.err.println("发送活动信号失败: " + e.getMessage());
			}
		}, 0, 30, TimeUnit.SECONDS); // 每60秒一次
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