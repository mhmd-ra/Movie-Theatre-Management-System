import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ManagerDashboard {
    private Stage primaryStage;
    private User currentUser;

    public ManagerDashboard(Stage primaryStage, User currentUser){
        this.primaryStage=primaryStage;
        this.currentUser=currentUser;
    }

    public void show() {

        Label title = new Label("Manager Dashboard");
        Label welcome = new Label("Welcome " + currentUser.getFirstName());

        Button scheduleBtn = new Button("Schedule Movie");
        Button reportBtn = new Button("Generate Report");
        Button maintenanceBtn = new Button("Maintenance");
        Button logoutBtn = new Button("Logout");

        scheduleBtn.setOnAction(e -> {
            System.out.println("Go to schedule page");
        });

        reportBtn.setOnAction(e -> {
            System.out.println("Go to reports");
        });

        maintenanceBtn.setOnAction(e -> {
            System.out.println("Go to maintenance");
        });

        logoutBtn.setOnAction(e -> {
            new UserLogin(primaryStage).initializeComponents();
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(title, welcome, scheduleBtn, reportBtn, maintenanceBtn, logoutBtn);

        Scene scene = new Scene(layout, 300, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


}
