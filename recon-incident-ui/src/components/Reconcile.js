import React, { useState } from "react";
import axios from "axios";
import { TextField, Button, Typography, Box } from "@mui/material";
import config from './config'; 
const Reconcile = () => {
  const [formData, setFormData] = useState({ account: "" });
  const [responseMessage, setResponseMessage] = useState(""); 
  const [loading, setLoading] = useState(false); 

  const apiUrl = config.reconcileUrl; 

  const handleInputChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  // Handle form submission
  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {

      const response = await axios.get(apiUrl);
      setResponseMessage(response.data);
    } catch (error) {
      console.error("Error submitting data:", error);
      setResponseMessage("Error submitting data. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ padding: 2 }}>
      
      <form onSubmit={handleFormSubmit}>
        <TextField
          label="CustomerId"
          name="account"
          value={formData.account}
          onChange={handleInputChange}
          fullWidth
          margin="normal"
        />
        
        <Button type="submit" variant="contained" color="primary" disabled={loading}>
          {loading ? "Submitting..." : "Submit"}
        </Button>
      </form>

      
      {responseMessage && (
        <Typography variant="body1" sx={{ marginTop: 2 }}>
          <strong>Response: </strong>{responseMessage}
        </Typography>
      )}
    </Box>
  );
};

export default Reconcile;
