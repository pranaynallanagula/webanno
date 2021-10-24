package de.tudarmstadt.ukp.clarin.webanno.support.dialog;

import java.io.Serializable;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.clarin.webanno.support.lambda.AjaxCallback;
import de.tudarmstadt.ukp.clarin.webanno.support.lambda.AjaxPayloadCallback;
import de.tudarmstadt.ukp.clarin.webanno.support.lambda.LambdaAjaxButton;
import de.tudarmstadt.ukp.clarin.webanno.support.lambda.LambdaAjaxLink;

public class RenameConfirmationDialog
        extends ModalWindow
{
    private static final long serialVersionUID = 5194857538069045172L;

    private IModel<String> titleModel;
    private IModel<String> contentModel;

    private AjaxPayloadCallback<String> confirmAction;
    private AjaxCallback cancelAction;

    private ContentPanel contentPanel;

    public RenameConfirmationDialog(String aId)
    {
        this(aId, null, null, null);
        titleModel = new StringResourceModel("title", this, null);
        contentModel = new StringResourceModel("text", this, null);
    }
    public RenameConfirmationDialog(String aId, IModel<String> aTitle)
    {
        this(aId, aTitle, Model.of());
    }

    public RenameConfirmationDialog(String aId, IModel<String> aTitle, IModel<String> aContent)
    {
        super(aId);

        titleModel = aTitle;
        contentModel = aContent;

        setOutputMarkupId(true);
        setInitialWidth(620);
        setInitialHeight(440);
        setResizable(true);
        setWidthUnit("px");
        setHeightUnit("px");
        setCssClassName("w_blue w_flex");
        showUnloadConfirmation(false);

        setModel(new CompoundPropertyModel<>(null));

        setContent(contentPanel = new ContentPanel(getContentId(), getModel()));

        setCloseButtonCallback((_target) -> {
            onCancelInternal(_target);
            return true;
        });
    }

    public void setModel(IModel<State> aModel)
    {
        setDefaultModel(aModel);
    }

    @SuppressWarnings("unchecked")
    public IModel<State> getModel()
    {
        return (IModel<State>) getDefaultModel();
    }

    public void setModelObject(State aModel)
    {
        setDefaultModelObject(aModel);
    }

    public State getModelObject()
    {
        return (State) getDefaultModelObject();
    }

    public IModel<String> getTitleModel()
    {
        return titleModel;
    }

    public void setTitleModel(IModel<String> aTitleModel)
    {
        titleModel = aTitleModel;
    }

    public IModel<String> getContentModel()
    {
        return contentModel;
    }

    public void setContentModel(IModel<String> aContentModel)
    {
        contentModel = aContentModel;
    }

    @Override
    public void show(IPartialPageRequestHandler aTarget)
    {
        contentModel.detach();

        State state = new State();
        state.content = contentModel.getObject();
        state.response = null;
        state.feedback = null;
        setModelObject(state);

        setTitle(titleModel.getObject());

        super.show(aTarget);
    }

    public AjaxPaylodCallback<String> getConfirmAction()
    {
        return confirmAction;
    }

    public void setConfirmAction(AjaxPaylodCallback<String> aConfirmAction)
    {
        confirmAction = aConfirmAction;
    }

    public AjaxCallback getCancelAction()
    {
        return cancelAction;
    }

    public void setCancelAction(AjaxCallback aCancelAction)
    {
        cancelAction = aCancelAction;
    }

    protected void onConfirmInternal(AjaxRequestTarget aTarget, Form<State> aForm)
    {
        State state = aForm.getModelObject();

        if(state.response == null){
            state.feedback = "Should not be empty.";
            atarget.add(aForm);
            return;
        }

        boolean closeOk = true;

        // Invoke callback if one is defined
        if (confirmAction != null) {
            try {
                confirmAction.accept(aTarget, state.response);
            }
            catch (Exception e) {
                LoggerFactory.getLogger(getPage().getClass()).error("Error: " + e.getMessage(), e);
                state.feedback = "Error: " + e.getMessage();
                aTarget.add(aForm);
                closeOk = false;
            }
        }

        if (closeOk) {
            close(aTarget);
        }
    }


    protected void onCancelInternal(AjaxRequestTarget aTarget)
    {
        if (cancelAction != null) {
            try {
                cancelAction.accept(aTarget);
            }
            catch (Exception e) {
                LoggerFactory.getLogger(getPage().getClass()).error("Error: " + e.getMessage(), e);
                contentPanel.form.getModelObject().feedback = "Error: " + e.getMessage();
            }
        }
        close(aTarget);
    }

    private class State
            implements Serializable
    {
        private static final long serialVersionUID = 4483229579553569947L;

        private String content;
        private String response;
        private String feedback;
    }

    private class ContentPanel
            extends Panel
    {
        private static final long serialVersionUID = 5202661827792148838L;

        private FeedbackPanel feedbackPanel;
        private Form<State> form;

        public ContentPanel(String aId, IModel<State> aModel)
        {
            super(aId, aModel);

            form = new Form<>("form", aModel);
            form.add(new Label("content").setEscapeModelStrings(false));
            form.add(new Label("feedback"));
            form.add(new TextField<>("response"));
            form.add(new LambdaAjaxButton<>("confirm",
                    RenameConfirmationDialog.this::onConfirmInternal));
            form.add(new LambdaAjaxLink("cancel", RenameConfirmationDialog.this::onCancelInternal));

            add(form);
        }
    }
}