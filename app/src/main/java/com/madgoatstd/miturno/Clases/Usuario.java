package com.madgoatstd.miturno.Clases;

public class Usuario {
    String Rut;
    private String pass;

    public Usuario(String rut, String pass) {
        Rut = rut;
        this.pass = pass;
    }

    public Usuario() {
    }

    public String getRut() {
        return Rut;
    }

    public void setRut(String rut) {
        Rut = rut;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}
