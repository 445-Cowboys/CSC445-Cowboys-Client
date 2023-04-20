module com.csc445cowboys.guiwip {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.csc445cowboys.guiwip to javafx.fxml;
    exports com.csc445cowboys.guiwip;
}