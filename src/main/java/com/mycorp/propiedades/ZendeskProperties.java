package com.mycorp.propiedades;

public class ZendeskProperties {

    ZendeskProperties(){

    }

    public static String getnPolizas() {
        return N_POLIZAS;
    }

    public static String getnTarj() {
        return N_TARJ;
    }

    public static String getTipoDoc() {
        return TIPO_DOC;
    }

    public static String getnDoc() {
        return N_DOC;
    }

    public static String getnMov() {
        return N_MOV;
    }

    public static String getUser() {
        return USER;
    }

    public static String getEmail() {
        return EMAIL;
    }
    private final static String N_POLIZAS = "Nº de poliza/colectivo: ";
    private final static String N_TARJ = "Nº tarjeta Sanitas o Identificador: ";
    private final static String TIPO_DOC = "Tipo documento: ";
    private final static String N_DOC = "Nº documento: ";
    private final static String N_MOV = "Nº móvil: ";

    private final static String USER = "User Agent: ";

    private final static String EMAIL = "Email Persona: ";


}
