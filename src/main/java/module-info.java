module com.example.backpropagationfx {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.backpropagationfx to javafx.fxml;
    exports com.example.backpropagationfx;
}