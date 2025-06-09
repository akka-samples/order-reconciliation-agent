import React, { useState } from "react";
import axios from "axios";
import { TextField, Button, Typography, Box } from "@mui/material";
import { Bar } from "react-chartjs-2";
import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale } from 'chart.js';
import config from './config'; 

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale);

const Simulation = () => {
  const [formData, setFormData] = useState({
    nrOfAddressIssues: 0,
    nrOfPaymentMismatchIssues: 0,
    nrOfReconciliationBeforePaymentIssues: 0,
  });
  const [responseMessage, setResponseMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [graphData, setGraphData] = useState({
    labels: ["Total Issues"],
    datasets: [
      {
        label: 'Sum of Issues',
        data: [0],
        backgroundColor: '#3b82f6',
      },
    ],
  });


  const handleInputChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: parseInt(e.target.value, 10) || 0, 
    });
  };

  // Simulate API call when button is pressed
  const handleSimulate = async (e) => {
    e.preventDefault();
    setLoading(true);
    const { nrOfAddressIssues, nrOfPaymentMismatchIssues, nrOfReconciliationBeforePaymentIssues } = formData;
    const totalIssues = nrOfAddressIssues + nrOfPaymentMismatchIssues + nrOfReconciliationBeforePaymentIssues;
    
    try {
      const response = await axios.post(config.apiUrl, {
        "nrOfAddressIssues": nrOfAddressIssues,
        "nrOfPaymentMismatchIssues": nrOfPaymentMismatchIssues,
        "nrOfReconciliationBeforePaymentIssues": nrOfReconciliationBeforePaymentIssues
      });

      
      setResponseMessage(`Simulation complete. Total issues: ${totalIssues}`);
      updateGraphData(totalIssues);
    } catch (error) {
      console.error("Error during simulation:", error);
      setResponseMessage("Error simulating API call. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  // Update graph data based on the sum of issues 
  const updateGraphData = (totalIssues) => {
    setGraphData({
      labels: ["Total Issues"],
      datasets: [
        {
          label: 'Sum of Issues',
          data: [totalIssues],
          backgroundColor: '#3b82f6',
        },
      ],
    });
  };

  return (
    <Box sx={{ padding: 2 }}>
      <Typography variant="h6" gutterBottom>Simulate Incidents</Typography>

      {/* Form */}
      <form onSubmit={handleSimulate}>
        <TextField
          label="Address Missing"
          name="nrOfAddressIssues"
          type="number"
          value={formData.nrOfAddressIssues}
          onChange={handleInputChange}
          fullWidth
          margin="normal"
        />
        <TextField
          label="Invoice and Payment Mismatch"
          name="nrOfPaymentMismatchIssues"
          type="number"
          value={formData.nrOfPaymentMismatchIssues}
          onChange={handleInputChange}
          fullWidth
          margin="normal"
        />
        <TextField
          label="Reconciliation Before Payment"
          name="nrOfReconciliationBeforePaymentIssues"
          type="number"
          value={formData.nrOfReconciliationBeforePaymentIssues}
          onChange={handleInputChange}
          fullWidth
          margin="normal"
        />
        
        <Button
          type="submit"
          variant="contained"
          color="primary"
          disabled={loading}
        >
          {loading ? "Simulating..." : "Simulate"}
        </Button>
      </form>

      {responseMessage && (
        <Typography variant="body1" sx={{ marginTop: 2 }}>
          <strong>Response: </strong>{responseMessage}
        </Typography>
      )}

      {/* Not need. as simulation is moved to backend */}
      {/* <Box sx={{ marginTop: 3 }}>
        <Bar
          data={graphData}
          options={{
            responsive: true,
            plugins: {
              title: {
                display: true,
                text: 'Issue Simulation Results',
              },
            },
            scales: {
              x: {
                title: {
                  display: true,
                  text: 'Issues',
                },
              },
              y: {
                title: {
                  display: true,
                  text: 'Total Number of Issues',
                },
                beginAtZero: true,
              },
            },
          }}
        />
      </Box> */}
    </Box>
  );
};

export default Simulation;
