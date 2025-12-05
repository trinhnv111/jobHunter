package trinhnv.jobOKO.controller;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trinhnv.jobOKO.common.annotation.ApiMessage;
import trinhnv.jobOKO.domain.request.CompanyDTO;
import trinhnv.jobOKO.domain.entity.Company;
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
    public ResponseEntity<ResultPaginationResponse<CompanyDTO>> getAllCompany(
            @Filter Specification<Company> spec,
            Pageable pageable
            ){
        return ResponseEntity.ok(companyService.getAllCompany(pageable, spec));
    }

    @GetMapping("/company/{id}")
    @ApiMessage("Lấy danh sách công ty theo ID thành công")
    public ResponseEntity<CompanyDTO> getCompanyById(@PathVariable Long id){
        return ResponseEntity.ok().body( this.companyService.getCompanyById(id));
    }

    @PostMapping("/company")
    @ApiMessage("Thêm công ty thành công")
    public ResponseEntity<CompanyDTO> createCompany(@Valid @RequestBody CompanyDTO company) {
        return ResponseEntity.ok().body(this.companyService.handleCreateCompany(company));
    }

    @PutMapping("/company/{id}")
    @ApiMessage("Cập nhật công ty thành công")
    public ResponseEntity<CompanyDTO> updateCompany(@PathVariable Long id , @Valid @RequestBody CompanyDTO company) {
        return ResponseEntity.ok().body(this.companyService.handleUpdateCompany(id,company));
    }

}
