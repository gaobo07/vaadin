/*
 * Copyright 2000-2014 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.client.ui.loginform;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.FormPanel;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractSingleComponentContainerConnector;
import com.vaadin.client.ui.VTextField;
import com.vaadin.client.ui.button.ButtonConnector;
import com.vaadin.client.ui.nativebutton.NativeButtonConnector;
import com.vaadin.client.ui.textfield.TextFieldConnector;
import com.vaadin.shared.Connector;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.loginform.LoginFormRpc;
import com.vaadin.shared.ui.loginform.LoginFormState;

@Connect(com.vaadin.ui.LoginForm.class)
public class LoginFormConnector extends
        AbstractSingleComponentContainerConnector {

    private VTextField passwordField;
    private VTextField userField;
    private LoginFormRpc loginFormRpc;

    @Override
    public void updateCaption(ComponentConnector connector) {

    }

    @Override
    public VLoginForm getWidget() {
        return (VLoginForm) super.getWidget();
    }

    @Override
    protected void init() {
        super.init();

        loginFormRpc = getRpcProxy(LoginFormRpc.class);
        getWidget().addSubmitCompleteHandler(
                new FormPanel.SubmitCompleteHandler() {
                    @Override
                    public void onSubmitComplete(
                            FormPanel.SubmitCompleteEvent event) {
                        loginFormRpc.submitCompleted();
                    }
                });
    }

    @Override
    public LoginFormState getState() {
        return (LoginFormState) super.getState();
    }

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent event) {
        ComponentConnector content = getContent();
        if (content != null) {
            getWidget().setWidget(getContentWidget());
        }
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        LoginFormState state = getState();
        userField = configureTextField(state.userNameFieldConnector, "username");
        passwordField = configureTextField(state.passwordFieldConnector,
                "password");
        addSubmitButtonClickHandler(state.loginButtonConnector);
    }

    private VTextField configureTextField(Connector connector, String id) {
        if (connector != null) {
            VTextField textField = ((TextFieldConnector) connector).getWidget();

            textField.addKeyDownHandler(new SubmitKeyHandler());

            Element element = textField.getElement();
            String externalId = element.getId();
            if (externalId == null || externalId.isEmpty()
                    || externalId.startsWith("gwt-")) {
                element.setId(id);
            }
            DOM.setElementAttribute(element, "name", id);
            DOM.setElementAttribute(element, "autocomplete", "on");

            return textField;
        } else {
            return null;
        }
    }

    private void login() {
        getWidget().submit();
        valuesChanged();
        loginFormRpc.submitted();
    }

    private void addSubmitButtonClickHandler(Connector buttonConnector) {
        if (buttonConnector instanceof ButtonConnector) {
            addSubmitButtonClickHandler(((ButtonConnector) buttonConnector)
                    .getWidget());
        } else if (buttonConnector instanceof NativeButtonConnector) {
            addSubmitButtonClickHandler(((NativeButtonConnector) buttonConnector)
                    .getWidget());
        }
    }

    private void addSubmitButtonClickHandler(FocusWidget button) {
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                login();
            }
        });
    }

    private void valuesChanged() {
        if (passwordField != null) {
            passwordField.valueChange(true);
        }
        if (userField != null) {
            userField.valueChange(true);
        }
    }

    private class SubmitKeyHandler implements KeyDownHandler {

        private int previousKeyCode;

        @Override
        public void onKeyDown(KeyDownEvent event) {
            int keyCode = event.getNativeKeyCode();
            if (keyCode == KeyCodes.KEY_ENTER) {
                if (isInAutoComplete()) {
                    previousKeyCode = keyCode;
                } else {
                    login();
                }
            } else {
                previousKeyCode = keyCode;
            }
        }

        private boolean isInAutoComplete() {
            switch (previousKeyCode) {
            case KeyCodes.KEY_PAGEUP:
            case KeyCodes.KEY_PAGEDOWN:
            case KeyCodes.KEY_UP:
            case KeyCodes.KEY_DOWN:
                return true;
            default:
                return false;
            }
        }
    }
}