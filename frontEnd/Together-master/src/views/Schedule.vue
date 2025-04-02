<template>
  <div class="spreadsheet-container">
    <gc-spread-sheets class="spread-host" @workbookInitialized="initSpread">
    </gc-spread-sheets>
    <div class="options-container">
      <div class="option-row">
        <div class="inputContainer">
          <p>Open Excel File (.xlsx)</p>
          <input type="file" id="fileDemo" class="input" @change="changeFileDemo" />
          <input type="button" id="loadExcel" value="Open Excel" class="button" @click="loadExcel" />
          <p>Add Data</p>
          <input type="button" id="saveExcel" value="Add Revenue" class="button" @click="modifyExcel" />

          <p>Save Excel File (.xlsx)</p>
          <input id="exportFileName" value="export.xlsx" class="input" @change="changeExportFileName" />
          <input type="button" id="saveExcel" value="Save Excel" class="button" @click="saveExcel" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import "@mescius/spread-sheets/styles/gc.spread.sheets.excel2016colorful.css";
import * as GC from "@mescius/spread-sheets";
import "@mescius/spread-sheets-io";
import {saveAs} from "file-saver";

export default {
  name: "App",
  methods: {
    // Initialize the Vue Spreadsheet Instance
    initSpread: function (spread) {
      this.spread = spread;
      let sheet = this.spread.getActiveSheet();
      this.revenueCount = 8;
      this.newRowIndex = 11;
    },
    changeFileDemo(e) {
      this.importExcelFile = e.target.files[0];
    },
    changeExportFileName(e) {
      this.exportFileName = e.target.value;
    },
    loadExcel() {
      let spread = this.spread;
      let excelFile = this.importExcelFile;
      let options = {
        fileType: GC.Spread.Sheets.FileType.excel,
      };
      // Import an existing Excel file to Vue spreadsheet
      spread.import(
          excelFile,
          () => {
            console.log("Import successful");
          },
          (e) => {
            console.error("Error during import:", e);
          },
          options
      );
    },
    modifyExcel() {
      let spread = this.spread;
      let sheet = spread.getActiveSheet();
      // Add a new row for the next revenue item
      sheet.addRows(this.newRowIndex, 1);
      // Copy styles from an existing row
      sheet.copyTo(10, 1, this.newRowIndex, 1, 1, 29, GC.Spread.Sheets.CopyToOptions.style);
      // Set the new row's first column with the revenue label
      var cellText = ("Revenue " + this.revenueCount++);
      sheet.setValue(this.newRowIndex, 1, cellText);
      // Fill the row with random revenue data
      for (var c = 3; c < 15; c++) {
        sheet.setValue(this.newRowIndex, c, Math.floor(Math.random() * 200) + 10);
      }
      // Add a sparkline chart for the revenue data
      var data = new GC.Spread.Sheets.Range(this.newRowIndex, 3, 1, 12);
      var setting = new GC.Spread.Sheets.Sparklines.SparklineSetting();
      setting.options.seriesColor = "Text 2";
      setting.options.lineWeight = 1;
      setting.options.showLow = true;
      setting.options.showHigh = true;
      setting.options.lowMarkerColor = "Text 2";
      setting.options.highMarkerColor = "Text 1";
      sheet.setSparkline(this.newRowIndex, 2, data, GC.Spread.Sheets.Sparklines.DataOrientation.horizontal, GC.Spread.Sheets.Sparklines.SparklineType.line, setting);
      // Add a formula to calculate the sum for the new row's year data
      sheet.setFormula(this.newRowIndex, 15, "=SUM([@[Jan]:[Dec]])");
      // Move to the next row for future modifications
      this.newRowIndex++;
      // Set a fixed value in the next column
      sheet.setValue(this.newRowIndex, 16, 0.15);
      // Copy formula settings to other parts of the row
      sheet.copyTo(10, 17, this.newRowIndex, 17, 1, 13, GC.Spread.Sheets.CopyToOptions.formula);
    },
    saveExcel() {
      let spread = this.spread;
      var fileName = "Excel_Export.xlsx";
      // Save Vue spreadsheet to local Excel XLSX file
      spread.export(
          function (blob) {
            // save blob to a file
            saveAs(blob, fileName);
          },
          function (e) {
            console.log(e);
          },
          {
            fileType: GC.Spread.Sheets.FileType.excel,
          }
      );
    },
  },
};
</script>

<style>
.spreadsheet-container {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

.spread-host {
  flex-grow: 1;
  width: 75%;
  height: 600px;
}

.options-container {
  width: 20%;
  padding: 20px;
  background: "#ddd";
}

.inputContainer {
  border: 1px solid #eee;
  padding: 20px;
  background-color: #f0f0f0;
  box-sizing: border-box;
  height: 600px;
}

</style>
