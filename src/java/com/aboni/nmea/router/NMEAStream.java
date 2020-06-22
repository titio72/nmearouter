/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router;

public interface NMEAStream {

    /**
     * Push a sentence into the stream.
     *
     * @param msg The message to be distributed.
     * @see com.aboni.nmea.router.agent.NMEASource
     */
    void pushSentence(RouterMessage msg);

    /**
     * Subscribe to NMEA stream.
     * The observer will be called back for each sentence on methods annotated with OnSentence.
     * The argument of the method annotated with OnSentence can be either a JSONObject or a Sentence.
     * @param observer The object to be registered as observer.
     * @see com.aboni.nmea.router.OnSentence
     */
    void subscribe(Object observer);

    /**
     * Remove the subscription to the stream.
     * @param observer The observer to be removed.
     * @see NMEAStream#subscribe(Object observer)
     */
    void unsubscribe(Object observer);

}
