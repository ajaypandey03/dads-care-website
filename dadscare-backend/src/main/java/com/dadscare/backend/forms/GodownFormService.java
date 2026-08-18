package com.dadscare.backend.forms;

import com.dadscare.backend.masterdata.ProductMaster;
import com.dadscare.backend.masterdata.ProductMasterRepository;
import com.dadscare.backend.masterdata.TransporterMaster;
import com.dadscare.backend.masterdata.TransporterMasterRepository;
import com.dadscare.backend.tenant.Organization;
import com.dadscare.backend.unlock.CreateUnlockRequestRequest;
import com.dadscare.backend.unlock.UnlockRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists the operational form data (stock/trucks/labor/custom fields) submitted
 * alongside an {@link UnlockRequest} — see Godown Operational Workflow in Confluence.
 * Called from {@code UnlockRequestService} within the same transaction as the unlock
 * request itself.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GodownFormService {

    private final GodownFormRepository godownFormRepository;
    private final StockLineRepository stockLineRepository;
    private final TruckEntryRepository truckEntryRepository;
    private final ProductMasterRepository productMasterRepository;
    private final TransporterMasterRepository transporterMasterRepository;
    private final ObjectMapper objectMapper;

    /** No-op (returns null) if the request carries no form data at all — an empty row is just noise. */
    @Transactional
    public GodownForm createIfPresent(UnlockRequest unlockRequest, Organization organization, CreateUnlockRequestRequest request) {
        boolean hasStock = request.stockLines() != null && !request.stockLines().isEmpty();
        boolean hasTrucks = request.truckEntries() != null && !request.truckEntries().isEmpty();
        boolean hasLabor = request.laborCount() != null;
        boolean hasRemarks = request.remarks() != null && !request.remarks().isBlank();
        boolean hasCustomFields = request.customFields() != null && !request.customFields().isEmpty();

        if (!hasStock && !hasTrucks && !hasLabor && !hasRemarks && !hasCustomFields) {
            return null;
        }

        GodownForm form = new GodownForm();
        form.setUnlockRequest(unlockRequest);
        form.setOrganization(organization);
        form.setLaborCount(request.laborCount());
        form.setRemarks(request.remarks());
        form.setCustomFieldsJson(serializeCustomFields(request.customFields()));
        godownFormRepository.save(form);

        if (hasStock) {
            for (var line : request.stockLines()) {
                ProductMaster product = requireProduct(organization.getId(), line.productMasterId());
                StockLine stockLine = new StockLine();
                stockLine.setGodownForm(form);
                stockLine.setProduct(product);
                stockLine.setQuantity(line.quantity());
                stockLine.setUnit(product.getUnit());
                stockLineRepository.save(stockLine);
            }
        }

        if (hasTrucks) {
            for (var entry : request.truckEntries()) {
                ProductMaster product = requireProduct(organization.getId(), entry.productMasterId());
                TransporterMaster transporter = requireTransporter(organization.getId(), entry.transporterMasterId());
                TruckEntry truckEntry = new TruckEntry();
                truckEntry.setGodownForm(form);
                truckEntry.setSource(entry.source());
                truckEntry.setProduct(product);
                truckEntry.setVehicleNo(entry.vehicleNo());
                truckEntry.setTransporter(transporter);
                truckEntry.setQuantity(entry.quantity());
                truckEntry.setWaitingSince(entry.waitingSince());
                truckEntryRepository.save(truckEntry);
            }
        }

        return form;
    }

    @Transactional(readOnly = true)
    public List<StockLine> stockLinesFor(Long godownFormId) {
        return stockLineRepository.findAllByGodownFormId(godownFormId);
    }

    @Transactional(readOnly = true)
    public List<TruckEntry> truckEntriesFor(Long godownFormId) {
        return truckEntryRepository.findAllByGodownFormId(godownFormId);
    }

    private ProductMaster requireProduct(Long organizationId, Long id) {
        return productMasterRepository
                .findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("ProductMaster " + id + " not found"));
    }

    private TransporterMaster requireTransporter(Long organizationId, Long id) {
        return transporterMasterRepository
                .findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("TransporterMaster " + id + " not found"));
    }

    private String serializeCustomFields(List<CreateUnlockRequestRequest.CustomFieldRequest> customFields) {
        if (customFields == null || customFields.isEmpty()) {
            return null;
        }
        try {
            List<Map<String, String>> asMaps = customFields.stream()
                    .map(f -> Map.of("heading", f.heading(), "value", f.value()))
                    .toList();
            return objectMapper.writeValueAsString(asMaps);
        } catch (Exception e) {
            log.warn("Failed to serialize custom fields, dropping them for this form: {}", e.getMessage());
            return null;
        }
    }
}
