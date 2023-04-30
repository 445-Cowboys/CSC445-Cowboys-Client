module com.csc445cowboys.guiwip {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.crypto.tink;


    opens com.csc445cowboys.guiwip to javafx.fxml;
    exports com.csc445cowboys.guiwip;
    exports com.csc445cowboys.guiwip.Controllers;
    opens com.csc445cowboys.guiwip.Controllers to javafx.fxml;
    exports com.csc445cowboys.guiwip.Net;
    opens com.csc445cowboys.guiwip.Net to javafx.fxml;
}