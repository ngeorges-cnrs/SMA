/* Copyright CNRS-CREATIS
 *
 * Rafael Ferreira da Silva
 * rafael.silva@creatis.insa-lyon.fr
 * http://www.rafaelsilva.com
 *
 * This software is a grid-enabled data-driven workflow manager and editor.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
package fr.insalyon.creatis.sma.server.execution;

import fr.insalyon.creatis.sma.common.Communication;
import fr.insalyon.creatis.sma.common.Constants;
import fr.insalyon.creatis.sma.common.ExecutorConstants;
import fr.insalyon.creatis.sma.server.execution.command.SendEmailCommand;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 *
 * @author Rafael Ferreira da Silva
 */
public class Executor extends Thread {

    private static final Logger logger = Logger.getLogger(Executor.class);
    private Communication communication;

    public Executor(Communication communication) {
        this.communication = communication;
    }

    @Override
    public void run() {
        try {
            String message = communication.getMessage();
            if (message != null) {
                Command command = parseCommand(message);

                if (command != null) {
                    command.execute();
                }
            } else {
                logException(new Exception("Error during message receive: " + message));
            }
        } catch (IOException ex) {
            logger.error(ex);
        } finally {
            try {
                communication.close();
            } catch (IOException ex) {
                logger.error(ex);
            }
        }
    }

    /**
     * 
     * @param message
     * @return 
     */
    private Command parseCommand(String message) {

        try {
            String[] tk = message.split(Constants.MSG_SEP_1);
            int command = Integer.parseInt(tk[0]);

            switch (command) {

                case ExecutorConstants.MESSAGEPOOL_ADD_OPERATION:
                    return new SendEmailCommand(communication, tk[1], tk[2], tk[3], tk[4], tk[5]);

                default:
                    logException(new Exception("Command not recognized: " + message));
            }

        } catch (NumberFormatException ex) {
            logException(new Exception("Invalid command: " + ex.getMessage()));
        } catch (ArrayIndexOutOfBoundsException ex) {
            logException(new Exception("Wrong number of parameters."));
        }
        return null;
    }

    /**
     * 
     * @param ex 
     */
    private void logException(Exception ex) {

        communication.sendErrorMessage(ex.getMessage());
        communication.sendEndOfMessage();

        logger.error(ex.getMessage());
        if (logger.isDebugEnabled()) {
            for (StackTraceElement stack : ex.getStackTrace()) {
                logger.debug(stack);
            }
        }
    }
}
