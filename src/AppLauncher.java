import javax.swing.SwingUtilities;


public class AppLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                //Display Gui
                // new WeatherAppGui().setVisible(true);

                System.out.println(WeatherApp.getLocationData("Tokyo"));
            }
        });
    }
}
