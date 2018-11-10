package org.apache.fineract.portfolio.mpesa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class BankReadPlatfomService {

	private final JdbcTemplate jdbcTemplate;
	private final BankNameMapper bankNameMapper;

	@Autowired
	public BankReadPlatfomService(final RoutingDataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.bankNameMapper = new BankNameMapper();
	}
	
	public Collection<BankName> retrieveBankName() {
            final String sql = "select fb.bank_id as bankId ,fb.bank_name as bankName from f_bank_name_mpesa fb";
            return this.jdbcTemplate.query(sql, this.bankNameMapper, new Object[] {});
    }
	
	private static final class BankNameMapper implements RowMapper<BankName>{

		@Override
		public BankName mapRow(ResultSet rs, int rowNum) throws SQLException {
			String bankId = rs.getString("bankId");
			String bankName = rs.getString("bankName");
			return new BankName(bankId, bankName);
		}	
	}
}
