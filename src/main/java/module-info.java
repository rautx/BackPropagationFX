module com.example.backpropagationfx {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.backpropagationfx to javafx.fxml;
    opens com.example.backpropagationfx.visao to javafx.fxml;
    opens com.example.backpropagationfx.modelo to javafx.fxml;

    exports com.example.backpropagationfx;
    exports com.example.backpropagationfx.visao;
    exports com.example.backpropagationfx.modelo;
}
