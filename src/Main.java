public class Main {
    public static void main(String[] args) {
        Persistence.DemoDataInitializer.initialize(new Management.FileSystem());

        // launch GUI
        GUI.MainWindow.main(args);
    }
}