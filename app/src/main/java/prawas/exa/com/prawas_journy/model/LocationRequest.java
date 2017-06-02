package prawas.exa.com.prawas_journy.model;

import java.io.Serializable;

/**
 * Created by root on 1/6/17.
 */

public class LocationRequest implements Serializable{
    Double lattitude;
    Double longitude;

    public Double getLattitude() {
        return lattitude;
    }

    public void setLattitude(Double lattitude) {
        this.lattitude = lattitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
