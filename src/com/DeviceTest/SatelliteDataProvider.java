package com.DeviceTest;

/**
 * Copyright (C) 2009 Ludwig M Brinckmann <ludwigbrinckmann@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Satellite information data provider
 * 
 * @author mtk54046
 * @version 1.0
 */
public interface SatelliteDataProvider {
    // !!== Enlarge maxSatellites from 15 to 24 for AGPS usage ==
    // !!== Enlarge maxSatellites from 24 to max for multi-GNSS ==
    int MAX_SATELLITES_NUMBER = 256;
    int SATELLITES_MASK_SIZE = 8;
    int SATELLITES_MASK_BIT_WIDTH = 32;

    /**
     * Set satellite status data
     * 
     * @param svCount
     *            Current satellites count
     * @param prns
     *            All of the satellites' PRN
     * @param snrs
     *            All of the satellites' SNR
     * @param elevations
     *            All of the satellites' elevation
     * @param azimuths
     *            All of the satellites' azimuth
     * @param ephemerisMask
     *            Ephemeris mask
     * @param almanacMask
     *            Almanac mask
     * @param usedInFixMask
     *            Used in fix
     */
    void setSatelliteStatus(int svCount, int[] prns, float[] snrs,
            float[] elevations, float[] azimuths, int ephemerisMask,
            int almanacMask, int[] usedInFixMask);

    /**
     * Get satellite status data
     * 
     * @param prns
     *            All of the satellites' PRN
     * @param snrs
     *            All of the satellites' SNR
     * @param elevations
     *            All of the satellites' elevation
     * @param azimuths
     *            All of the satellites' azimuth
     * @param ephemerisMask
     *            Ephemeris mask
     * @param almanacMask
     *            Almanac mask
     * @param usedInFixMask
     *            Used in fix
     * @return Satellites count
     */
    int getSatelliteStatus(int[] prns, float[] snrs, float[] elevations,
            float[] azimuths, int ephemerisMask, int almanacMask,
            int[] usedInFixMask);
};
