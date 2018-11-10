package org.apache.fineract.portfolio.mpesa;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class BankPropertyReadPlatfomService {

	private final JdbcTemplate jdbcTemplate;
	private final BankPropertyMapper bankNameMapper;

	@Autowired
	public BankPropertyReadPlatfomService(final RoutingDataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.bankNameMapper = new BankPropertyMapper();
	}
	
	public BankProperty retrieveBankPropertDescription(String propertyDescription) {
            final String sql = "select fb.property_name as propertyName ,fb.property_description as propertyDescription from f_bank_mpesa_details fb where fb.property_description = ?";
            return this.jdbcTemplate.queryForObject(sql, this.bankNameMapper, new Object[] {propertyDescription});
    }
	
	public BankProperty retrieveBankPropertName(String propertyName) {
        final String sql = "select fb.property_name as propertyName ,fb.property_description as propertyDescription from f_bank_mpesa_details fb where fb.property_name = ?";
        return this.jdbcTemplate.queryForObject(sql, this.bankNameMapper, new Object[] {propertyName});
}
	
	private static final class BankPropertyMapper implements RowMapper<BankProperty>{

		@Override
		public BankProperty mapRow(ResultSet rs, int rowNum) throws SQLException {
			String propertyName = rs.getString("propertyName");
			String propertyDescription = rs.getString("propertyDescription");
			return new BankProperty(propertyName, propertyDescription);
		}	
	}

	public JdbcTemplate getJdbcTemplate() {
		return this.jdbcTemplate;
	}

	public BankPropertyMapper getBankNameMapper() {
		return this.bankNameMapper;
	}
}
