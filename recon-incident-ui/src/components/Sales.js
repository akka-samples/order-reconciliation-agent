import React, { useState, useEffect } from "react";
import axios from "axios";
import TabContent from "./TabContent";
import config from './config'; 
const Sales = () => {
  const [tableData, setTableData] = useState([]);

  const apiUrl = config.salesUrl;
  
  const handleFormSubmit = async (data) => {
    try {
      await axios.post(apiUrl, data);
  
      fetchTableData();
    } catch (error) {
      console.error("Error submitting data:", error);
    }
  };


  const fetchTableData = async () => {
    try {
      const response = await axios.get(apiUrl);
      setTableData(response.data); 
    } catch (error) {
      console.error("Error fetching data:", error);
    }
  };

  useEffect(() => {
    fetchTableData();
  }, []);

  const formFields = [
    { name: "invoiceId", label: "Invoice Id" },
    { name: "customerId", label: "Customer Id" },
    { name: "amount", label: "Amount" },
    { name: "status", label: "Status" },
    { name: "date", label: "Date" },
  ];


  // todo: this works only in alphabatical order@@
  const tableHeaders = ["Amount", "Customer Id", "Date",  "Invoice Id","Status"];

  return (
    <TabContent
      formFields={formFields}
      tableHeaders={tableHeaders}
      tableData={tableData}
      onSubmit={handleFormSubmit}
    />
  );
};

export default Sales;
