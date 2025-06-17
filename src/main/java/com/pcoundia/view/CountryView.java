package com.pcoundia.view;

public class CountryView {

    public interface CyRead extends View.Public, View.ReadOnly {}

    public interface CyReadDetail extends CyRead {}

    public interface CyWrite extends View.Public {}
}
