package offline.export.pictureViewer;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

@SuppressWarnings("serial")
public class ViewerAction extends AbstractAction {
	private String actionName = "";
	private ViewerFrame frame = null;

	// 这个工具栏的AbstractAction所对应的org.crazyit.viewer.action包的某个Action实全
	private IAction action = null;

	public ViewerAction() {

		super();
	}

	public ViewerAction(ImageIcon icon, String actionName, ViewerFrame frame) {
		// 调用父构造器
		super("", icon);
		this.actionName = actionName;
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		ViewerService service = ViewerService.getInstance();
		IAction action = getAction(this.actionName);
		// 调用Action的execute方法
		action.execute(service, frame);
	}

	private IAction getAction(String actionName) {
		try {
			if (this.action == null) {
				// 创建Action实例
				IAction action = (IAction) Class.forName(actionName).newInstance();
				this.action = action;
			}
			return this.action;
		} catch (Exception e) {
			return null;
		}
	}

}