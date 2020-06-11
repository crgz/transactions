package com.github.crgz.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * Read data in an Excel spreadsheet and return it as a collection of objects.
 * This is designed to facilitate for parameterized tests in JUnit that
 * get data from an excel spreadsheet.
 * 
 * @author Conrado M.
 */
public class WorkbookData
{
	private final HSSFWorkbook workbook;
	private final int index;

	private WorkbookData(Builder builder)
	{
		this.workbook = builder.workbook;
		this.index = builder.index;
	}

	public static final class Builder
	{
		private final HSSFWorkbook workbook;
		private int index = 0;

		public Builder(String path) throws IOException
		{
			this.workbook = new HSSFWorkbook(new FileInputStream(path));
		}

		public Builder sheet(final int index)
		{
			this.index = index;
			return this;
		}

		public WorkbookData build() {
			return new WorkbookData(this);
		}
	}

	/**
	 * The contents of the spreadsheet, in a form compatible with JUnit 4 parameterized tests.
	 */
	public Collection<Object[]> collection() throws WorkbookDataException
	{
		Sheet sheet = this.workbook.getSheetAt(this.index);

		List<Object[]> rows = new ArrayList<>();
		List<Object> rowData = new ArrayList<>();

		for (Row row : sheet)
		{
			if (!isEmpty(row))
			{
				rowData.clear();
				for (int column = 0; column < countNonEmptyColumns(sheet); column++)
				{
					Cell cell = row.getCell(column);
					rowData.add(objectFrom(workbook, cell));
				}
				rows.add(rowData.toArray());
			}
			else
			{
				break;
			}
		}
		return rows;
	}

	private boolean isEmpty(final Row row)
	{
		Cell firstCell = row.getCell(0);
		return (firstCell == null) || (firstCell.getCellTypeEnum() == CellType.BLANK);
	}

	/**
	 * Count the number of columns, using the number of non-empty cells in the
	 * first row.
	 */
	private int countNonEmptyColumns(final Sheet sheet)
	{
		int columnCount = 0;
		for (Cell cell : sheet.getRow(0))
		{
			if (cell.getCellTypeEnum() == CellType.BLANK)
			{
				return columnCount;
			}
			columnCount++;
		}
		return columnCount;
	}

	private Object objectFrom(final HSSFWorkbook workbook, final Cell cell) throws WorkbookDataException
	{
		switch (cell.getCellTypeEnum())
		{
		case STRING:
			return cell.getRichStringCellValue().getString();
		case NUMERIC:
			return getNumericCellValue(cell);
		case BOOLEAN:
			return cell.getBooleanCellValue();
		case FORMULA:
			return evaluateCellFormula(workbook, cell);
		default:
			throw new WorkbookDataException();
		}
	}

	private Object getNumericCellValue(final Cell cell)
	{
		return DateUtil.isCellDateFormatted(cell) ? new Date(cell.getDateCellValue().getTime()) : cell.getNumericCellValue();
	}

	private Object evaluateCellFormula(final HSSFWorkbook workbook, final Cell cell) throws WorkbookDataException
	{
		CellValue cellValue = workbook.getCreationHelper().createFormulaEvaluator().evaluate(cell);
		switch (cellValue.getCellTypeEnum())
		{
		case BOOLEAN:
			return cellValue.getBooleanValue();
		case NUMERIC:
			return cellValue.getNumberValue();
		case STRING:
			return cellValue.getStringValue();
		default:
			throw new WorkbookDataException();
		}
	}
}
