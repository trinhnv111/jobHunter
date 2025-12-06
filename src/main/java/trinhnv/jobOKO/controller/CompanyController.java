package trinhnv.jobOKO.controller;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trinhnv.jobOKO.common.annotation.ApiMessage;
import trinhnv.jobOKO.domain.request.CompanyRequest;
import trinhnv.jobOKO.domain.entity.Company;
import trinhnv.jobOKO.domain.response.CompanyResponse;
import trinhnv.jobOKO.service.CompanyService;
import trinhnv.jobOKO.domain.response.ResultPaginationResponse;

@RestController
public class CompanyController {
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/company")
    @ApiMessage("Lấy danh sách công ty thành công")
    public ResponseEntity<ResultPaginationResponse<CompanyResponse>> getAllCompany(
            @Filter Specification<Company> spec,
            Pageable pageable
            ){
        return ResponseEntity.ok(companyService.getAllCompany(pageable, spec));
    }

    @GetMapping("/company/{id}")
    @ApiMessage("Lấy danh sách công ty theo ID thành công")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable Long id){
        return ResponseEntity.ok().body( this.companyService.getCompanyById(id));
    }

    @PostMapping("/company")
    @ApiMessage("Thêm công ty thành công")
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.ok().body(this.companyService.handleCreateCompany(request));
    }

    @PutMapping("/company/{id}")
    @ApiMessage("Cập nhật công ty thành công")
    public ResponseEntity<CompanyResponse> updateCompany(@PathVariable Long id , @Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.ok().body(this.companyService.handleUpdateCompany(id, request));
    }

    @DeleteMapping("/company/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id){
        this.companyService.deleteCompany(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
