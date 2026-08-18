package com.dadscare.backend.masterdata;

import com.dadscare.backend.tenant.Organization;
import com.dadscare.backend.tenant.OrganizationRepository;
import com.dadscare.backend.tenant.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD for {@link ProductMaster} and {@link TransporterMaster}, both scoped by
 * {@link TenantContext#organizationId()} on every operation — never by an id the
 * caller supplies.
 */
@Service
@RequiredArgsConstructor
public class MasterDataService {

    private final ProductMasterRepository productMasterRepository;
    private final TransporterMasterRepository transporterMasterRepository;
    private final OrganizationRepository organizationRepository;

    // ---- Product masters ----

    @Transactional(readOnly = true)
    public List<ProductMasterDto> listProducts() {
        return productMasterRepository.findAllByOrganizationIdAndActiveTrue(TenantContext.organizationId()).stream()
                .map(ProductMasterDto::from)
                .toList();
    }

    @Transactional
    public ProductMasterDto createProduct(ProductMasterDto dto) {
        Organization org = currentOrganization();
        ProductMaster entity = new ProductMaster();
        entity.setOrganization(org);
        entity.setName(dto.name());
        entity.setUnit(dto.unit());
        entity.setActive(true);
        return ProductMasterDto.from(productMasterRepository.save(entity));
    }

    @Transactional
    public ProductMasterDto updateProduct(Long id, ProductMasterDto dto) {
        ProductMaster entity = productMasterRepository
                .findByIdAndOrganizationId(id, TenantContext.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("ProductMaster " + id + " not found"));
        entity.setName(dto.name());
        entity.setUnit(dto.unit());
        entity.setActive(dto.active());
        return ProductMasterDto.from(entity);
    }

    @Transactional
    public void deactivateProduct(Long id) {
        ProductMaster entity = productMasterRepository
                .findByIdAndOrganizationId(id, TenantContext.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("ProductMaster " + id + " not found"));
        entity.setActive(false);
    }

    // ---- Transporter masters ----

    @Transactional(readOnly = true)
    public List<TransporterMasterDto> listTransporters() {
        return transporterMasterRepository
                .findAllByOrganizationIdAndActiveTrue(TenantContext.organizationId())
                .stream()
                .map(TransporterMasterDto::from)
                .toList();
    }

    @Transactional
    public TransporterMasterDto createTransporter(TransporterMasterDto dto) {
        Organization org = currentOrganization();
        TransporterMaster entity = new TransporterMaster();
        entity.setOrganization(org);
        entity.setName(dto.name());
        entity.setCode(dto.code());
        entity.setActive(true);
        return TransporterMasterDto.from(transporterMasterRepository.save(entity));
    }

    @Transactional
    public TransporterMasterDto updateTransporter(Long id, TransporterMasterDto dto) {
        TransporterMaster entity = transporterMasterRepository
                .findByIdAndOrganizationId(id, TenantContext.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("TransporterMaster " + id + " not found"));
        entity.setName(dto.name());
        entity.setCode(dto.code());
        entity.setActive(dto.active());
        return TransporterMasterDto.from(entity);
    }

    @Transactional
    public void deactivateTransporter(Long id) {
        TransporterMaster entity = transporterMasterRepository
                .findByIdAndOrganizationId(id, TenantContext.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("TransporterMaster " + id + " not found"));
        entity.setActive(false);
    }

    private Organization currentOrganization() {
        return organizationRepository
                .findById(TenantContext.organizationId())
                .orElseThrow(() -> new IllegalStateException("Authenticated organization no longer exists"));
    }
}
