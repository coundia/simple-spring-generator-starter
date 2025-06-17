package com.pcoundia.view;

import org.junit.jupiter.api.Test;

class CountryViewTest {

    @Test
    void it_should_load_view_classes() {
        Class<?> read = CountryView.CyRead.class;
        Class<?> readDetail = CountryView.CyReadDetail.class;
        Class<?> write = CountryView.CyWrite.class;
        assert read != null;
        assert readDetail != null;
        assert write != null;
    }
}
