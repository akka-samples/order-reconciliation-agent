import React, { useState, useEffect } from "react";
import axios from "axios";
import TabContent from "./TabContent";
import config from './config'; 

const Payment = () => {
  const [tableData, setTableData] = useState([]);

  const apiUrl = config.paymentUrl; 

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
    { name: "id", label: "Id" },
    { name: "invoiceId", label: "Invoice Id" },
    { name: "customerId", label: "Customer Id" },
    { name: "amount", label: "Amount" },
    { name: "paymentDate", label: "Payment Date" },
    { name: "billingAddress", label: "Billing Address" },
    { name: "postalCode", label: "Postal Code" },
  ];


  const tableHeaders = ["Amount", "Billing Address", "Customer Id", "Id", "Invoice Id", "Payment Date", "Postal Code"];

  return (
    <TabContent
      formFields={formFields}
      tableHeaders={tableHeaders}
      tableData={tableData}
      onSubmit={handleFormSubmit}
    />
  );
};

export default Payment;
