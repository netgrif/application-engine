package com.netgrif.application.engine.workflow.web.responsebodies;

public class ResponseMessageCode {

    public static final String success = "forms.success";
    public static final String error = "forms.error";

    /**
     * Registration
     */
    public static final String registrationSuccess = "forms.register.success";
    public static final String registrationSuccessChangePassword = "forms.register.successChangePassword";
    public static final String registrationSuccessSendMail = "forms.register.successSendMail";
    public static final String registrationSuccessRegistration = "forms.register.successRegistration";
    public static final String registrationError = "forms.register.error";
    public static final String registrationErrorSendMail = "forms.register.errorSendMail";
    public static final String registrationErrorInvalidToken = "forms.register.errorInvalidToken";
    public static final String registrationErrorInvalidTokenForMail = "forms.register.errorInvalidTokenForMail";
    public static final String registrationErrorNotFoundUser = "forms.register.errorNotFoundUser";
    public static final String registrationErrorOnlyAdmin = "forms.register.errorOnlyAdmin";

}
