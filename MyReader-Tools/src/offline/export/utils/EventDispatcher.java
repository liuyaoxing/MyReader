package offline.export.utils;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * 事件转发中心 如果需要获得异常通知，可以实现 <code>PropertyChangeListener</code>接口 并调用{
 * {@link #addPropertyChangeListener(PropertyChangeListener)}方法
 * 将自己注册成为接收者，在发生异常时，所有注册成为接收者的类将收到异常通知
 */
public class EventDispatcher {

	/** 属性修改支持实现类 */
	private static PropertyChangeSupportImpl instance = new PropertyChangeSupportImpl();

	/**
	 * 分发消息 。
	 * 
	 * @param propName
	 *            事件的属性名称 在处理的时候会根据属性名称作相应的处理
	 * @param obj
	 *            属性值，用于描述......
	 * @param source
	 *            由哪个类发出的属性修改事件 数据源
	 */
	public static void dispatchMessage(String propName, Object newValue, Object oldValue) {
		instance.firePropertyChange(propName, oldValue, newValue);
	}

	/**
	 * 添加属性事件 。
	 * 
	 * @param l
	 */
	public static void addPropertyChangeListener(PropertyChangeListener l) {
		instance.addPropertyChangeListener(l);
	}

	/**
	 * 删除属性事件 。
	 * 
	 * @param l
	 */
	public static void removePropertyChangeListener(PropertyChangeListener l) {
		instance.removePropertyChangeListener(l);
	}

	/**
	 * 属性修改支持实现类 。
	 */
	public static class PropertyChangeSupportImpl {

		/** 属性修改支持 */
		PropertyChangeSupport listeners = new PropertyChangeSupport(this);

		/**
		 * 添加属性修改事件 。
		 * 
		 * @param l
		 */
		public void addPropertyChangeListener(PropertyChangeListener l) {
			listeners.addPropertyChangeListener(l);
		}

		/**
		 * 通知修改事件发生 。
		 * 
		 * @param prop
		 * @param oldValue
		 * @param newValue
		 */
		public void firePropertyChange(String prop, Object oldValue, Object newValue) {
			listeners.firePropertyChange(prop, oldValue, newValue);
		}

		/**
		 * 通知事件修改 。
		 * 
		 * @param prop
		 * @param child
		 */
		protected void fireStructureChange(String prop, Object child) {
			listeners.firePropertyChange(prop, null, child);
		}

		/**
		 * 删除属性修改事件 。
		 * 
		 * @param l
		 */
		public void removePropertyChangeListener(PropertyChangeListener l) {
			listeners.removePropertyChangeListener(l);
		}
	}

}
