package mx.infornet.sgcoach;

public class Planes {

    private int id, id_gimnasio;
    private String nombre;
    private double precio;
    private String servicios;

    public Planes(int id, String nombre, double precio, String servicios) {
        this.id = id;
        //this.id_gimnasio = id_gimnasio;
        this.nombre = nombre;
        this.precio = precio;
        this.servicios = servicios;
    }

    public int getId() {
        return id;
    }

    public int getId_gimnasio() { return  id_gimnasio; }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() { return precio;}

    public String getServicios() {
        return servicios;
    }

}
