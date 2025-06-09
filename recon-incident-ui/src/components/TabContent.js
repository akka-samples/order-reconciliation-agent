import React, { useState } from "react";
import { Box, Tab, Tabs, TextField, Button, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper } from "@mui/material";

const TabContent = ({ formFields, tableHeaders, tableData, onSubmit }) => {
  const [formData, setFormData] = useState(formFields.reduce((acc, field) => {
    acc[field.name] = "";
    return acc;
  }, {}));

  const handleInputChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(formData);
    setFormData(formFields.reduce((acc, field) => {
      acc[field.name] = "";
      return acc;
    }, {})); // Reset the form
  };

  return (
    <Box sx={{ padding: 2 }}>
      <form onSubmit={handleSubmit}>
        {formFields.map((field, index) => (
          <TextField
            key={index}
            label={field.label}
            name={field.name}
            value={formData[field.name]}
            onChange={handleInputChange}
            fullWidth
            margin="normal"
          />
        ))}
        <Button type="submit" variant="contained" color="primary">
          Submit
        </Button>
      </form>

      <TableContainer component={Paper} sx={{ marginTop: 2 }}>
        <Table>
          <TableHead>
            <TableRow>
              {tableHeaders.map((header, index) => (
                <TableCell key={index}>{header}</TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {tableData.map((row, index) => (
              <TableRow key={index}>
                {Object.values(row).map((value, idx) => (
                  <TableCell key={idx}>{value}</TableCell>
                ))}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

export default TabContent;
