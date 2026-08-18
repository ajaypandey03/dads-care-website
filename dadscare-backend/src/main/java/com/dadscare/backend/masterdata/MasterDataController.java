package com.dadscare.backend.masterdata;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MasterDataController {

    private final MasterDataService masterDataService;

    @GetMapping("/product-masters")
    public List<ProductMasterDto> listProducts() {
        return masterDataService.listProducts();
    }

    @PostMapping("/product-masters")
    public ProductMasterDto createProduct(@Valid @RequestBody ProductMasterDto dto) {
        return masterDataService.createProduct(dto);
    }

    @PutMapping("/product-masters/{id}")
    public ProductMasterDto updateProduct(@PathVariable Long id, @Valid @RequestBody ProductMasterDto dto) {
        return masterDataService.updateProduct(id, dto);
    }

    @DeleteMapping("/product-masters/{id}")
    public void deactivateProduct(@PathVariable Long id) {
        masterDataService.deactivateProduct(id);
    }

    @GetMapping("/transporter-masters")
    public List<TransporterMasterDto> listTransporters() {
        return masterDataService.listTransporters();
    }

    @PostMapping("/transporter-masters")
    public TransporterMasterDto createTransporter(@Valid @RequestBody TransporterMasterDto dto) {
        return masterDataService.createTransporter(dto);
    }

    @PutMapping("/transporter-masters/{id}")
    public TransporterMasterDto updateTransporter(@PathVariable Long id, @Valid @RequestBody TransporterMasterDto dto) {
        return masterDataService.updateTransporter(id, dto);
    }

    @DeleteMapping("/transporter-masters/{id}")
    public void deactivateTransporter(@PathVariable Long id) {
        masterDataService.deactivateTransporter(id);
    }
}
