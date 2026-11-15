import java.time.LocalDate;

public abstract class Usuario extends Persona {
    private String correo;
    private String password;

    public Usuario(String nombre, String documento, LocalDate fechaNacimiento, String correo, String password) {
        super(nombre, documento, fechaNacimiento);
        this.correo = correo;
        this.password = password;
    }

    public boolean autenticar(String correo, String password) {
        return this.correo.equalsIgnoreCase(correo) && this.password.equals(password);
    }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}