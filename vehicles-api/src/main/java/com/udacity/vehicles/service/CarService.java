package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.Address;
import com.udacity.vehicles.client.maps.MapsClient;
import com.udacity.vehicles.client.prices.PriceClient;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {

    private final CarRepository repository;
    private final PriceClient priceClient;

    public CarService(CarRepository repository, MapsClient mapsClient, PriceClient priceClient) {
        /**
         * Add the Maps and Pricing Web Clients you create
         *   in `VehiclesApiApplication` as arguments and set them here.
         */
        this.repository = repository;
        this.priceClient = priceClient;
    }

    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        return repository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        /**
         * Find the car by ID from the `repository` if it exists.
         *   If it does not exist, throw a CarNotFoundException
         *   Remove the below code as part of your implementation.
         */
        Optional<Car> optionCar = repository.findById(id);
        if (optionCar.isEmpty()) {
            throw new CarNotFoundException();
        }
        Car car = optionCar.get();

        /**
         * Use the Pricing Web client you create in `VehiclesApiApplication`
         *   to get the price based on the `id` input'
         * Set the price of the car
         * Note: The car class file uses @transient, meaning you will need to call
         *   the pricing service each time to get the price.
         */
        String price = priceClient.getPrice(id);
        car.setPrice(price);

        /**
         * Use the Maps Web client you create in `VehiclesApiApplication`
         *   to get the address for the vehicle. You should access the location
         *   from the car object and feed it to the Maps service.
         * Set the location of the vehicle, including the address information
         * Note: The Location class file also uses @transient for the address,
         * meaning the Maps service needs to be called each time for the address.
         */
        double lat = car.getLocation().getLat();
        double lon = car.getLocation().getLat();
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = String.format("http://localhost:9191/maps?lat=%f&lon=%f", lat, lon);
        ResponseEntity<Address> response
                = restTemplate.getForEntity(fooResourceUrl + "/1", Address.class);
        Address address = response.getBody();
        car.setAddress(address);

        return car;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            return repository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        carToBeUpdated.setCondition(car.getCondition());
                        return repository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }

        return repository.save(car);
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        /**
         * Find the car by ID from the `repository` if it exists.
         *   If it does not exist, throw a CarNotFoundException
         */
        Optional<Car> optionCar = repository.findById(id);
        if (optionCar.isEmpty()) {
            throw new CarNotFoundException();
        }
        Car car = optionCar.get();

        /**
         * Delete the car from the repository.
         */
        repository.delete(car);
    }
}
