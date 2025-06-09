import React, { useState, useEffect } from "react";
import axios from "axios";
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper } from "@mui/material";
import config from './config'; 

const Incident = () => {
  const [tableData, setTableData] = useState([]);
  const [openModal, setOpenModal] = useState(false);
  const [selectedIncident, setSelectedIncident] = useState(null);
  const [agentData, setAgentData] = useState([]); 
  
  const apiUrl = config.incidentUrl;
  // update to platform or library.
  const agentApiUrl = config.agentApiUrlPlatform;
  //const agentApiUrl = config.agentApiUrlLibrary;

  const fetchTableData = async () => {
    try {
      const response = await axios.get(apiUrl);
      
      setTableData(response.data);
    } catch (error) {
      console.error("Error fetching incidents:", error);
    }
  };

  const fetchAgentData = async (incidentId) => {
    try {
      const response = await axios.get(`${agentApiUrl}${incidentId}`);
      console.log(response);
      setAgentData(response.data.conversations);
    } catch (error) {
      console.error("Error fetching agent data:", error);
    }
  };

  useEffect(() => {
    fetchTableData();
  }, []);

  const handleOpenModal = (incident) => {
    setSelectedIncident(incident);
    fetchAgentData(incident.processingReference); 
    setOpenModal(true);
  };

  const handleCloseModal = () => {
    setOpenModal(false);
    setSelectedIncident(null);
    setAgentData([]); 
  };


  const formatAgentData = () => {
    return agentData.map((agent, index) => (
      <p key={index}>
        <strong>{agent.role}:</strong> {agent.content}
      </p>
    ));
  };


  const tableHeaders = ["Incident Id", "Description", "AI Resolution Reference"];

  return (
    <div>
      
      <TableContainer component={Paper} sx={{ backgroundColor: "#1a1a1a" }}>
        <Table>
          <TableHead>
            <TableRow>
              {tableHeaders.map((header, index) => (
                <TableCell key={index}>{header}</TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {tableData.map((incident, index) => (
              <TableRow key={index} onClick={() => handleOpenModal(incident)}>
                <TableCell  sx={{ width: '20%' }}>{incident.id}</TableCell>
                <TableCell>{incident.message}</TableCell>
                <TableCell sx={{ width: '25%' }}>{incident.processingReference}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={openModal} onClose={handleCloseModal} fullWidth>
        <DialogTitle>Incident Details</DialogTitle>
        <DialogContent>
          {selectedIncident && (
            <div>
              <p><strong>Incident Id:</strong> {selectedIncident.id}</p>
              <p><strong>Description:</strong> {selectedIncident.message}</p>
              <p><strong>Reference:</strong> {selectedIncident.processingReference}</p>
              <div>
                <h3>Agent Conversations:</h3>
                { formatAgentData()} {/* Render the formatted agent data */}
              </div>
            </div>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseModal} color="primary">
            Close
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
};

export default Incident;
