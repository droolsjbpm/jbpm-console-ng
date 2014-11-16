package org.jbpm.console.ng.wi.client.editors.deployment.descriptor;

import java.util.List;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.guvnor.common.services.shared.validation.model.ValidationMessage;
import org.guvnor.structure.client.file.CommandWithCommitMessage;
import org.guvnor.structure.client.file.SaveOperationService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jbpm.console.ng.wi.client.editors.deployment.descriptor.type.DDResourceType;
import org.jbpm.console.ng.wi.dd.model.DeploymentDescriptorModel;
import org.jbpm.console.ng.wi.dd.service.DDEditorService;

import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;
import org.uberfire.ext.widgets.common.client.callbacks.HasBusyIndicatorDefaultErrorCallback;
import org.kie.workbench.common.widgets.client.popups.validation.ValidationPopup;
import org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants;
import org.kie.workbench.common.widgets.metadata.client.KieEditor;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.client.annotations.WorkbenchEditor;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartTitleDecoration;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.lifecycle.IsDirty;
import org.uberfire.lifecycle.OnClose;
import org.uberfire.lifecycle.OnMayClose;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.events.NotificationEvent;
import org.uberfire.workbench.model.menu.Menus;

@WorkbenchEditor(identifier = "org.kie.jbpmconsole.dd", supportedTypes = { DDResourceType.class }, priority = 101)
public class DeploymentDescriptorEditorPresenter extends KieEditor {
    @Inject
    private Caller<DDEditorService> ddEditorService;

    private DeploymentDescriptorView view;

    @Inject
    private DDResourceType type;

    @Inject
    private Event<NotificationEvent> notification;

    private DeploymentDescriptorModel model;

    @Inject
    public DeploymentDescriptorEditorPresenter( final DeploymentDescriptorView baseView ) {
        super( baseView );
        view = baseView;
    }

    @OnStartup
    public void onStartup( final ObservablePath path, final PlaceRequest place ) {
        ddEditorService.call().createIfNotExists(path);
        init( path, place, type );
    }

    protected void loadContent() {
        view.showLoading();
        ddEditorService.call(new RemoteCallback<DeploymentDescriptorModel>() {

            @Override
            public void callback( final DeploymentDescriptorModel content ) {
                //Path is set to null when the Editor is closed (which can happen before async calls complete).
                if ( versionRecordManager.getCurrentPath() == null ) {
                    return;
                }

                model = content;
                resetEditorPages( content.getOverview() );
                addSourcePage();

                view.setContent( content );

                view.hideBusyIndicator();
            }
        },
        getNoSuchFileExceptionErrorCallback()).load(versionRecordManager.getCurrentPath());
    }

    protected Command onValidate() {
        return new Command() {
            @Override
            public void execute() {
                ddEditorService.call( new RemoteCallback<List<ValidationMessage>>() {
                    @Override
                    public void callback( final List<ValidationMessage> results ) {
                        if ( results == null || results.isEmpty() ) {
                            notification.fire( new NotificationEvent( CommonConstants.INSTANCE.ItemValidatedSuccessfully(),
                                    NotificationEvent.NotificationType.SUCCESS ) );
                        } else {
                            ValidationPopup.showMessages(results);
                        }
                    }
                }, new DefaultErrorCallback() ).validate( versionRecordManager.getCurrentPath(),
                        model );
            }
        };
    }

    protected void save() {
        new SaveOperationService().save( versionRecordManager.getCurrentPath(),
                new CommandWithCommitMessage() {
                    @Override
                    public void execute( final String comment ) {
                        view.showSaving();
                        view.updateContent(model);
                        ddEditorService.call( getSaveSuccessCallback(),
                                new HasBusyIndicatorDefaultErrorCallback( view ) ).save( versionRecordManager.getCurrentPath(),
                                model,
                                metadata,
                                comment );
                    }
                }
        );
        concurrentUpdateSessionInfo = null;
    }

    @Override
    protected void onSourceTabSelected() {
        view.updateContent(model);
        ddEditorService.call( new RemoteCallback<String>() {
            @Override
            public void callback( String source ) {
                updateSource( source );
            }
        } ).toSource( versionRecordManager.getCurrentPath(), model );
    }

    @WorkbenchPartView
    public IsWidget getWidget() {
        return super.getWidget();
    }

    @OnClose
    public void onClose() {
        this.versionRecordManager.clear();
    }

    @IsDirty
    public boolean isDirty() {
        if ( isReadOnly ) {
            return false;
        }
        return ( view.isDirty() );
    }

    @OnMayClose
    public boolean checkIfDirty() {
        if ( isDirty() ) {
            return view.confirmClose();
        }
        return true;
    }

    @WorkbenchPartTitleDecoration
    public com.google.gwt.user.client.ui.IsWidget getTitle() {
        return super.getTitle();
    }

    @WorkbenchPartTitle
    public String getTitleText() {
        return super.getTitleText();
    }

    @WorkbenchMenu
    public Menus getMenus() {
        return menus;
    }

    protected void makeMenuBar() {
        menus = menuBuilder
                .addSave( versionRecordManager.newSaveMenuItem(new Command() {
                    @Override
                    public void execute() {
                        onSave();
                    }
                }))
                .addValidate(onValidate())
                .addNewTopLevelMenu(versionRecordManager.buildMenu())
                .build();
    }
}
