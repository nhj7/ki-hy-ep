package com.kiassist.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;

import com.kiassist.views.KIAssistView;

/**
 * KI Assist View 열기 액션
 */
public class OpenKIAssistAction implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;

    @Override
    public void run(IAction action) {
        try {
            window.getActivePage().showView(KIAssistView.ID);
        } catch (PartInitException e) {
            e.printStackTrace();
        }
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