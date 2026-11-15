import java.time.LocalDate;
import java.time.Period;

public abstract class Persona {
    private String nombre;
    private String documento;
    private LocalDate fechaNacimiento;

    public Persona(String nombre, String documento, LocalDate fechaNacimiento) {
        this.nombre = nombre;
        this.documento = documento;
        this.fechaNacimiento = fechaNacimiento;
    }

    public int calcularEdadAFecha(LocalDate fechaReferencia) {
        if (fechaNacimiento == null || fechaReferencia == null) return 0;
        return Period.between(fechaNacimiento, fechaReferencia).getYears();
    }

    public abstract String obtenerRolDescripcion();

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
}