/* GenericComposer.java

	Purpose:
		
	Description:
		
	History:
		Nov 21, 2007 6:22:00 PM , Created by robbiecheng

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under LGPL Version 3.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.util;

import org.zkoss.lang.Objects;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.GenericEventListener;
import org.zkoss.zk.ui.metainfo.ComponentInfo;

/**
 * <p>An abstract composer that you can extend and write intuitive onXxx event handler methods;
 * this class will registers onXxx events to the supervised component automatically.</p>
 * <p>The following is an example. The onOK and onCancel event listener is registered into 
 * the target main window automatically.</p>
 * 
 * <pre><code>
 * &lt;zscript>&lt;!-- both OK in zscript or a compiled Java class -->
 * public class MyComposer extends GenericComposer {
 *    public void onOK() {
 *        //doOK!
 *        //...
 *    }
 *    public void onCancel() {
 *        //doCancel
 *        //...
 *    } 
 * }
 * &lt;/zscript>
 *
 * &lt;window id="main" apply="MyComposer">
 *     ...
 * &lt;/window>
 * </code></pre>
 * <p>since 3.6.1, this composer would be assigned as a variable of the given component 
 * per the naming convention composed of the component id and composer Class name. e.g.
 * If the applied component id is "xwin" and this composer class is 
 * org.zkoss.MyComposer, then the variable name would be "xwin$MyComposer". You can
 * reference this composer with {@link Component#getVariable} or via EL as ${xwin$MyComposer}
 * of via annotate data binder as @{xwin$MyComposer}, etc. If this composer is the 
 * first composer applied to the component, a shorter variable name
 * composed of the component id and a String "composer" would be also available for use. 
 * Per the above example, you can also reference this composer with the name "xwin$composer"</p>
 *
 * <p>Since 3.6.2, this composer becomes serializable, so it is better to
 * declare members to be wired as transient, since it will re-write automatically
 * after the session has been activated.
 * 
 * @author robbiecheng
 * @since 3.0.1
 */
abstract public class GenericComposer extends GenericEventListener
implements Composer, ComposerExt, ComponentActivationListener,
java.io.Serializable {
	private static final long serialVersionUID = 20091006115555L;
	private String _applied; //id of the applied component (for serialization back)
	private transient boolean _everWillPassivate; //Bug #2873310. willPassivate only once
	private transient boolean _everDidActivate; //Bug #2873310. didActivate only once
	
	/**
	 * Registers onXxx events to the supervised component; a subclass that override
	 * this method should remember to call super.doAfterCompose(comp) or it will not 
	 * work.
	 */
	public void doAfterCompose(Component comp) throws Exception {
		//bind this GenericEventListener to the supervised component
		_applied = comp.getId();
		bindComponent(comp);
	}

	//since 3.6.1
	public ComponentInfo doBeforeCompose(Page page, Component parent,
			ComponentInfo compInfo) {
		//do nothing
		return compInfo;
	}

	//since 3.6.1
	public void doBeforeComposeChildren(Component comp) throws Exception {
		//assign this composer as a variable
		//feature #2778508
		Components.wireController(comp, this);
	}

	//since 3.6.1
	public boolean doCatch(Throwable ex) throws Exception {
		//do nothing
		return false;
	}

	//since 3.6.1
	public void doFinally() throws Exception {
		//do nothing
	}

	//ScopeActivationListener//
	/** Called when the component is going to passivate this object.
	 * Default: (since 3.6.3) unregister onXxx listeners of the applied component.
	 */
	public void willPassivate(Component comp) {
		//Bug #2873327. Unregister listener when session passivate
		if (comp != null && Objects.equals(_applied, comp.getId())) {
			if (_everWillPassivate) return; //Bug #2873310. willPassivate only once
			_everWillPassivate = true;

			unbindComponent(comp);
		}
	}
	
	/** Called when the component has activated this object back.
	 * Default: invokes {@link #doAfterCompose}.
	 * @since 3.6.2
	 */
	public void didActivate(Component comp) {
		try {
			if (comp != null && Objects.equals(_applied, comp.getId())) {
				if (_everDidActivate) return; //Bug #2873310. didActivate only once
				_everDidActivate = true;

				doAfterCompose(comp);
			}
		} catch (Throwable ex) {
			throw UiException.Aide.wrap(ex);
		}
	}
}
