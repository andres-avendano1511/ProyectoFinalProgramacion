import java.time.LocalDate;

public class Ocupante extends Persona {
    public Ocupante(String nombre, LocalDate fechaNacimiento) {
        super(nombre, "N/A", fechaNacimiento);
    }

    @Override
    public String obtenerRolDescripcion() {
        return "Ocupante";
    }

    public boolean pagaTarifa(LocalDate fechaEntrada) {
        return calcularEdadAFecha(fechaEntrada) >= 6;
    }
}
