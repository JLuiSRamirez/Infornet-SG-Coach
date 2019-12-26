package mx.infornet.sgcoach;

import org.json.JSONArray;

public class Planes {

    private int id, id_gimnasio;
    private String nombre;
    private double precio;
    private String servicios;
    private JSONArray services;

    public Planes(int id, String nombre, double precio, String servicios, JSONArray services) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.servicios = servicios;
        this.services = services;
    }

    public int getId() {
        return id;
    }

    public int getId_gimnasio() { return  id_gimnasio; }

    public JSONArray getServices() { return  services; }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() { return precio;}

    public String getServicios() {
        return servicios;
    }

}
