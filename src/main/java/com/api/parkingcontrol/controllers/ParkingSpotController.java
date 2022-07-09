package com.api.parkingcontrol.controllers;

import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.validation.Valid;

import org.hibernate.annotations.SortType;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.parkingcontrol.dtos.ParkingSpotDto;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.services.ParkingSpotService;

import net.bytebuddy.asm.Advice.OffsetMapping.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {
	
	final ParkingSpotService parkingSpotService;
	
	public ParkingSpotController(ParkingSpotService parkingSpotService) {
		this.parkingSpotService = parkingSpotService;
	}
	
	@GetMapping
	public ResponseEntity<Page<ParkingSpotModel>> getAllParkingSpots(@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable) {
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll(pageable));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Object> getOneParkingSpot(@PathVariable(value = "id") UUID id) {
		Optional<ParkingSpotModel> parkingSpot = parkingSpotService.findById(id);
		if(!parkingSpot.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking spot not found");
		}
		
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpot.get());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Object> deleteParkingSpot(@PathVariable(value = "id") UUID id) {
		Optional<ParkingSpotModel> parkingSpot = parkingSpotService.findById(id);
		if(!parkingSpot.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking spot not found");
		}
		parkingSpotService.delete(parkingSpot.get());
		return ResponseEntity.status(HttpStatus.OK).body("Parking spot deleted successfully");
	}
	
	@PostMapping
	public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto) {
		
		if(parkingSpotService.existsByLicensePlateCar(parkingSpotDto.getLicensePlateCar())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: License plate car is already in use!");
		}
		
		if(parkingSpotService.existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking spot is already in use!");
		}
		
		if(parkingSpotService.existsByApartmentAndBlock(parkingSpotDto.getApartment(), parkingSpotDto.getBlock())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking spot already registered for this apartment/block!");
		}
		
		var parkingSpotModel = new ParkingSpotModel();
		BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
		parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
		return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Object> updateParkingSpot(@PathVariable(value = "id") UUID id, @RequestBody @Valid ParkingSpotDto parkingSpotDto) {
		Optional<ParkingSpotModel> parkingSpot = parkingSpotService.findById(id);
		if(!parkingSpot.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking spot not found");
		}
		
		// Opção 1		
        // var parkingSpotModel = parkingSpot.get();
        // parkingSpotModel = updateParkingSpotData(parkingSpotModel, parkingSpotDto);
		
		// Opção 2
		ParkingSpotModel parkingSpotModel = new ParkingSpotModel();
		BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
		parkingSpotModel.setId(parkingSpot.get().getId());
		parkingSpotModel.setRegistrationDate(parkingSpot.get().getRegistrationDate());
		
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));
	}
	
	public ParkingSpotModel updateParkingSpotData(ParkingSpotModel parkingSpotModel, ParkingSpotDto parkingSpotDto) {
		parkingSpotModel.setParkingSpotNumber(parkingSpotDto.getParkingSpotNumber());
		parkingSpotModel.setLicensePlateCar(parkingSpotDto.getLicensePlateCar());
		parkingSpotModel.setModelCar(parkingSpotDto.getModelCar());
		parkingSpotModel.setBrandCar(parkingSpotDto.getBrandCar());
		parkingSpotModel.setColorCar(parkingSpotDto.getColorCar());
		parkingSpotModel.setResponsibleName(parkingSpotDto.getResponsibleName());
		parkingSpotModel.setApartment(parkingSpotDto.getApartment());
		parkingSpotModel.setBlock(parkingSpotDto.getBlock());
		return parkingSpotModel;
	}

}
