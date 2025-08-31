
/**
 * 설정 페이지 열기 액션
 */
package com.kiassist.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class SettingsAction implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;

    @Override
    public void run(IAction action) {
        PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
            window.getShell(),
            "com.kiassist.preferences.main",
            null,
            null);
        dialog.open();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // 선택 변경 시 동작 (비워둠)
    }

    @Override
    public void dispose() {
        // 정리 작업 (비워둠)
    }

    @Override
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }
}