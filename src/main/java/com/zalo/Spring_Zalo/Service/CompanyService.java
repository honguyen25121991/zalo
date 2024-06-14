package com.zalo.Spring_Zalo.Service;

import com.zalo.Spring_Zalo.Entities.Company;

public interface CompanyService {
    Company creeateCompany(Company company);

    Company updateCompany(Company company, Integer companyId);

    Company getCompanyById(Integer companyId);
}
