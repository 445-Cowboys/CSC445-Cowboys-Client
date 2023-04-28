module com.csc445cowboys.guiwip {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.crypto.tink;


    opens com.csc445cowboys.guiwip to javafx.fxml;
    exports com.csc445cowboys.guiwip;
}