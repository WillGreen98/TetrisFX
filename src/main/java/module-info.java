module org.bgw.tetrisfx {
    requires javafx.controls;
    requires javafx.fxml;

    exports org.bgw.tetrisfx;

    opens org.bgw.tetrisfx to
            javafx.fxml;

    exports org.bgw.tetrisfx.controller;

    opens org.bgw.tetrisfx.controller to
            javafx.fxml;

    exports org.bgw.tetrisfx.model;
    exports org.bgw.tetrisfx.view;
    exports org.bgw.tetrisfx.service;
}
