import java.time.LocalDate;

public interface Cotizable {
    long calcularValorTotal(LocalDate fechaEntrada, LocalDate fechaSalida, int personasQuePagan);
}