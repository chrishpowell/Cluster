/** ***************************************************************************\
 * Lunar
 * \**************************************************************************** */
package eu.discoveri.predikt.astronomy.library;

import java.util.Calendar;
//import java.util.GregorianCalendar;
//import java.util.TimeZone;

//import com.mhuss.Util.DateU;
//import com.mhuss.Util.Str;
/**
 * A class to hold the classical Lunar fundimental elements The member names
 * should be familiar to Meeus fans ;-)
 */
class LunarFundamentals
{
    double Lp;  // mean longitude of Moon
    double D;   // mean elongation of Moon
    double M;   // mean anomaly of Sun
    double Mp;  // mean anomaly of Moon
    double F;   // mean distance of moon from its ascending node
    double A1;  // argument 1 (used in calculations)
    double A2;  // argument 2 (used in calculations)
    double A3;  // argument 3 (used in calculations)
    double T;   // time in Julian centuries since J2000.0

    LunarFundamentals()
    {
        Lp = 0D;
        D = 0D;
        M = 0D;
        Mp = 0D;
        F = 0D;
        A1 = 0D;
        A2 = 0D;
        A3 = 0D;
        T = 0D;
    }
};

class LunarUnitCheck
{
    // Added for unit test accuracy check

    double E;
    double sumLatitudeTerms;
    double sumLongitudeTerms;
    double sumRangeTerms;
}

/**
 * A class that can calculate lunar fundmentals for any reasonable time.
 * <P>
 * Based on C code by Bill Gray (www.projectpluto.com)
 * <P>
 * This class calculates Lunar fundamentals, and also contains some functions
 * which depend on these fundimentals, including latitude, longitude & distance,
 * phase angle and illuminated fraction.
 * <P>
 * The lunar fundamentals <B>must</B> be calculated before calling most of the
 * functions in this class.
 */
public class Lunar
{
    /**
     * phase constant
     */
    public final static int NEW = 0, Q1 = 1, FULL = 2, Q3 = 3;

    /**
     * Default constructor
     */
    public Lunar() {
        m_LEs = new LocationElements();
        m_initialized = false;
    }

    /**
     * Date/time constructor.
     *
     * This constructor uses the time provided to calculate the lunar
     * fundamentals.
     *
     * @param t Time in Julian centuries ref J2000
     */
    public Lunar(double t) {
        m_LEs = new LocationElements();
        calcFundamentals(t);
    }

    //-------------------------------------------------------------------------
    // All formulae and "magic numbers" are from Meeus, Astronomical
    // Algorithms, 2ed
    /**
     * Calculates the lunar fundamanentals for a given time.
     *
     * @param t Time in Julian centuries referenced to J2000
     */
    public void calcFundamentals(double t) {
        if (null == m_f) {
            m_f = new LunarFundamentals();
        }

        m_f.Lp = getFund(LunarTerms.LunarFundimentals_Lp, t);
        m_f.D = getFund(LunarTerms.LunarFundimentals_D, t);
        m_f.M = getFund(LunarTerms.LunarFundimentals_M, t);
        m_f.Mp = getFund(LunarTerms.LunarFundimentals_Mp, t);
        m_f.F = getFund(LunarTerms.LunarFundimentals_F, t);

        m_f.A1 = toNormalizedRadians(119.75 + 131.849 * t);
        m_f.A2 = toNormalizedRadians(53.09 + 479264.290 * t);
        m_f.A3 = toNormalizedRadians(313.45 + 481266.484 * t);
        m_f.T = t;

        if (null == m_uc) {
            m_uc = new LunarUnitCheck();
        }

        // indicate values need to be recalculated
        m_LEs.invalidate();

        // set init'd flag to true
        m_initialized = true;
    }

//-------------------------------------------------------------------------
    /**
     * Calculate the phase angle in radians. <BR>
     * (Using Meeus' easy lower precision method).
     * <P>
     * NOTE: The lunar fundamentals must be calculated before calling this
     * function or a <code>NoInitException</code> will be thrown.
     *
     * @return The current phase angle in radians
     */
    public double phaseAngle() throws NoInitException {
        if (!m_initialized) {
            throw new NoInitException(NoInit);
        }

        return toNormalizedRadians(
                180 - Math.toDegrees(m_f.D)
                - 6.289 * Math.sin(m_f.Mp)
                + 2.110 * Math.sin(m_f.M)
                - 1.274 * Math.sin((2 * m_f.D) - m_f.Mp)
                - 0.658 * Math.sin(2 * m_f.D)
                - 0.214 * Math.sin(2 * m_f.Mp)
                - 0.110 * Math.sin(m_f.D)
        );
    }

    /**
     * Calculate the illuminated fraction.
     * <P>
     * NOTE: The lunar fundamentals must be calculated before calling this
     * function or a <code>NoInitException</code> will be thrown.
     *
     * @return The current illuminated fraction (0.0 to 1.0)
     */
    public double illuminatedFraction() throws NoInitException {
        if (!m_initialized) {
            throw new NoInitException(NoInit);
        }

        return (1D + Math.cos(phaseAngle())) / 2D;
    }

    /**
     * Calculate the time of the quarter nearest the given Julian day.
     *
     * @param jd A Julian day close to the expected date
     * @param quarter (<TT>Lunar.NEW</TT>, <TT>Q1</TT>, <TT>FULL</TT>, or
     * <TT>Q3</TT>)
     * @param cal Calendar to use for time zone adjustment; if null no time zone
     * adjustment is performed
     *
     * @return The relatively exact Julian day with decimal
     */
    public static double getPhase(long jd, int quarter, Calendar cal) {

        if (quarter < NEW) {
            quarter = NEW;
        }
        if (quarter > Q3) {
            quarter = Q3;
        }
        boolean newOrFull = (NEW == quarter || FULL == quarter);

        double T = AstroOps.toMillenia(jd);
        double k = Math.floor(T * 1236.85) + (double) quarter / 4;
        /*
     * double k = Math.floor(( y - 2000) * 12.3685);
     * double T = k/1236.85;
         */

        // eccentricity of Earth's orbit around the sun
        double E = 1 - (0.002516 * T) - (0.0000074 * T * T);

        // time of *mean* phase
        double JDE = 2451550.09766
                + LunarCalc.SYNODIC_MONTH * k
                + 0.00015437 * T * T
                - 0.000000150 * T * T * T
                + 0.00000000073 * T * T * T * T
                + .5;

        if (null != cal) {
            JDE += TimeOps.tzOffsetInDays(cal);
        }

        // Sun's mean anomaly
        double M = getFund(LunarTerms.PhaseFundimentals_M, k, T);
        // Moon's mean anomaly
        double Mp = getFund(LunarTerms.PhaseFundimentals_Mp, k, T);
        // Moon's argument of latitude
        double F = getFund(LunarTerms.PhaseFundimentals_F, k, T);
        // Longitude of the ascending node of the lunar orbit
        double Om = getFund(LunarTerms.PhaseFundimentals_Om, k, T);

        // calculate correction to *true* phase
        LunarTermsPh terms[];
        int nTerms;
        if (newOrFull) {
            terms = LunarTerms.LunarPhaseNF;
            nTerms = LunarTerms.LunarPhaseNF.length;
        } else {
            terms = LunarTerms.LunarPhaseQ;
            nTerms = LunarTerms.LunarPhaseQ.length;
        }

        // first group of periodic terms
        double correction = 0.;
        double sumOfPeriodicArguments;
        for (int i = 0; i < nTerms; i++) {

            sumOfPeriodicArguments = M * terms[i].m;
            sumOfPeriodicArguments += Mp * terms[i].mp;
            sumOfPeriodicArguments += F * terms[i].f;
            sumOfPeriodicArguments += Om * terms[i].om;

            double phasePeriodicTerm = Math.sin(sumOfPeriodicArguments) * terms[i].nm;
            for (int e = terms[i].e; e > 0; e--) {
                phasePeriodicTerm *= E;
            }

            correction += phasePeriodicTerm;
        }
        JDE += correction;

        if (!newOrFull) {
            // corrections for 1Q & 3Q only
            double W = 0.00306
                    - 0.00038 * E * Math.cos(M)
                    + 0.00026 * Math.cos(Mp)
                    - 0.00002 * Math.cos(Mp - M)
                    + 0.00002 * Math.cos(Mp + M)
                    + 0.00002 * Math.cos(F + F);

            JDE += (Q1 == quarter) ? W : -W;
        }

        // additional corrections for all phases
        JDE += 0.000325
                * Math.sin(Math.toRadians(299.77 + 0.107408 * k - 0.009173 * T * T)); // A1
        JDE += 0.000165
                * Math.sin(Math.toRadians(251.88 + 0.016321 * k));   // A2
        JDE += 0.000164
                * Math.sin(Math.toRadians(251.83 + 26.651886 * k));   // A3
        JDE += 0.000126
                * Math.sin(Math.toRadians(349.42 + 36.412478 * k));   // A4
        JDE += 0.000110
                * Math.sin(Math.toRadians(84.66 + 18.206239 * k));   // A5
        JDE += 0.000062
                * Math.sin(Math.toRadians(141.74 + 53.303772 * k));   // A6
        JDE += 0.000060
                * Math.sin(Math.toRadians(207.14 + 2.453732 * k));   // A7
        JDE += 0.000056
                * Math.sin(Math.toRadians(154.84 + 7.306860 * k));   // A8
        JDE += 0.000047
                * Math.sin(Math.toRadians(34.52 + 27.261239 * k));   // A9
        JDE += 0.000042
                * Math.sin(Math.toRadians(207.19 + 0.121824 * k));   // A10
        JDE += 0.000040
                * Math.sin(Math.toRadians(291.34 + 1.844379 * k));   // A11
        JDE += 0.000037
                * Math.sin(Math.toRadians(161.72 + 24.198154 * k));   // A12
        JDE += 0.000035
                * Math.sin(Math.toRadians(239.56 + 25.513099 * k));   // A13
        JDE += 0.000023
                * Math.sin(Math.toRadians(331.55 + 3.592518 * k));   // A14

        return JDE;
    }

    /**
     * Calculate the time of the quarter nearest the given Julian day.
     *
     * @param jd A Julian day close to the expected date
     * @param quarter (<TT>Lunar.NEW</TT>, <TT>Q1</TT>, <TT>FULL</TT>, or
     * <TT>Q3</TT>)
     *
     * @return The relatively exact Julian day with decimal
     */
    public static double getPhase(long jd, int quarter) {
        return getPhase(jd, quarter, null);
    }

    /**
     * Calculate the latitude in degrees.
     * <P>
     * NOTE: The lunar fundamentals must be calculated before calling this
     * function or a <code>NoInitException</code> will be thrown.
     *
     * @return The latitude in degrees
     */
    public double getLatitude() throws NoInitException {
        if (!m_initialized) {
            throw new NoInitException(NoInit);
        }

        double latitude = m_LEs.getLatitude();
        if (latitude < 0.) {
            LunarTermsLat tptr[] = LunarTerms.LunarLat;
            double sumLatitudeTerms = 0.;

            double e = 1. - .002516 * m_f.T - .0000074 * m_f.T * m_f.T;

            // for unit test
            m_uc.E = e;

            for (int i = 0; i < tptr.length; i++) {

                double sumOfPeriodicArguments;

                sumOfPeriodicArguments = tptr[i].d * m_f.D;
                sumOfPeriodicArguments += tptr[i].m * m_f.M;
                sumOfPeriodicArguments += tptr[i].mp * m_f.Mp;
                sumOfPeriodicArguments += tptr[i].f * m_f.F;

                double latitudePeriodicTerm
                        = tptr[i].sb * Math.sin(sumOfPeriodicArguments);

                /*
        * Terms containing the angle M depend on the eccentricity of the
        * Earth's orbit (which is decreasing slightly), so the amplitude of
        * these terms is actually variable.
        *
        * Multiply by e if M = 1 or -1; multiply by e * e if M = 2 or -2:
                 */
                for (int j = Math.abs(tptr[i].m); j != 0; j--) {
                    latitudePeriodicTerm *= e;
                }

                sumLatitudeTerms += latitudePeriodicTerm;
            }

            sumLatitudeTerms += -2235. * Math.sin(m_f.Lp)
                    + 382. * Math.sin(m_f.A3)
                    + 175. * Math.sin(m_f.A1 - m_f.F)
                    + 175. * Math.sin(m_f.A1 + m_f.F)
                    + 127. * Math.sin(m_f.Lp - m_f.Mp)
                    - 115. * Math.sin(m_f.Lp + m_f.Mp);

            // Added for unit test accuracy check
            m_uc.sumLatitudeTerms = sumLatitudeTerms;

            latitude = sumLatitudeTerms * 1.e-6;
            m_LEs.setLatitude(latitude);
        }

        return latitude;
    }

    /**
     * Calculate the lunar latitude in radians.
     * <P>
     * NOTE: The lunar fundamentals must be calculated before calling this
     * function or a <code>NoInitException</code> will be thrown.
     *
     * @return The lunar latitude in radians
     */
    public double getLatitudeRadians() throws NoInitException {
        if (!m_initialized) {
            throw new NoInitException(NoInit);
        }

        return Math.toRadians(getLatitude());
    }

    /**
     * Calculate the lunar longitude in degrees.
     * <P>
     * NOTE: The lunar fundamentals must be calculated before calling this
     * function or a <code>NoInitException</code> will be thrown.
     *
     * @return The lunar longitude in degrees
     */
    public double getLongitude() throws NoInitException {
        if (m_LEs.getLongitude() < 0.) {
            calcLonRad();
        }
        return m_LEs.getLongitude();
    }

    /**
     * Calculate the lunar longitude in radians.
     * <P>
     * NOTE: The lunar fundamentals must be calculated before calling this
     * function or a <code>NoInitException</code> will be thrown.
     *
     * @return The lunar longitude in radians
     */
    public double getLongitudeRadians() throws NoInitException {
        if (!m_initialized) {
            throw new NoInitException(NoInit);
        }

        return (Math.toRadians(getLongitude()));
    }

    /**
     * Calculate the lunar radius (distance).
     * <P>
     * NOTE: The lunar fundamentals must be calculated before calling this
     * function or a <code>NoInitException</code> will be thrown.
     *
     * @return The lunar radius
     */
    public double getRadius() throws NoInitException {
        if (m_LEs.getRadius() < 0.) {
            calcLonRad();
        }
        return m_LEs.getRadius();
    }

    //-------------------------------------------------------------------------
    /**
     * Calculate the fundamentals and then all three location elements for the
     * given time. This function calls <TT>callFundamentals()</TT>.
     *
     * @param locs Where the calculated LEs go
     * @param t time in decimal centuries
     */
    public void calcAllLEs(LocationElements locs,
            double t)
            throws NoInitException {
        calcFundamentals(t);
        locs.set(getLatitudeRadians(), getLongitudeRadians(), getRadius());
    }

    //-------------------------------------------------------------------------
    // PRIVATE methods
    //-------------------------------------------------------------------------
    /**
     * reduce a positive angle to (0 <= d < 360) and convert to radians
     */
    private static double toNormalizedRadians(double d) {
        return Math.toRadians(AstroOps.normalizeDegrees(d));
    }

    //-------------------------------------------------------------------------
    /**
     * calculate an individual fundimental
     *
     * @param fundArray - points to array of doubles
     * @param t - time in decimal Julian centuries
     */
    private static double getFund(double fundArray[], double t) {
        double d = fundArray[0];
        double tpow = t;    // tpow = T, T^2, T^3, ...
        for (int i = 1; i < 5; i++) {
            d += tpow * fundArray[i];
            tpow *= t;
        }
        return toNormalizedRadians(d);
    }

    /**
     * calculate an individual fundimental
     *
     * @param fundArray - points to array of doubles
     * @param k - phase constant, 0 = new moon Jan 6, 2000
     * @param t - time in decimal Julian centuries
     */
    private static double getFund(double fundArray[], double k, double t) {
        double d = fundArray[0] + k * fundArray[1];
        double tpow = t * t;    // tpow = T^2, T^3, ...
        for (int i = 2; i < 5; i++) {
            d += tpow * fundArray[i];
            tpow *= t;
        }
        return toNormalizedRadians(d);
    }
//-------------------------------------------------------------------------

    /**
     * Calculate the longitude and radius. Note that the longitude here is
     * relative to the Vernal Equinox, rather than being the geodetic longitude
     * (which differs by Greenwich sidereal time).
     *
     * NOTE: calcFundamentals() must have been called first
     */
    private void calcLonRad() throws NoInitException {

        if (!m_initialized) {
            m_LEs.setLongitude(Astro.INVALID);
            m_LEs.setRadius(Astro.INVALID);
            throw new NoInitException(NoInit);
        }

        LunarTermsLonRad tptr[] = LunarTerms.LunarLonRad;

        double sumLongitudeTerms = 0., sumRangeTerms = 0.;
        double e = 1. - .002516 * m_f.T - .0000074 * m_f.T * m_f.T;

        for (int i = 0; i < tptr.length; i++) {

            double sumOfPeriodicArguments;

            sumOfPeriodicArguments = tptr[i].d * m_f.D;
            sumOfPeriodicArguments += tptr[i].m * m_f.M;
            sumOfPeriodicArguments += tptr[i].mp * m_f.Mp;
            sumOfPeriodicArguments += tptr[i].f * m_f.F;

            double longitudePeriodicTerm = tptr[i].sl * Math.sin(sumOfPeriodicArguments);

            /*
    * Terms containing the angle M depend on the eccentricity of the Earth's
    * orbit (which is decreasing slightly), so the amplitude of these terms
    * is actually variable.
    *
    * Multiply by e if M = 1 or -1; multiply by e squared if M = 2 or -2:
             */
            for (int j = Math.abs(tptr[i].m); j != 0; j--) {
                longitudePeriodicTerm *= e;
            }

            sumLongitudeTerms += longitudePeriodicTerm;

            double rangePeriodicTerm = tptr[i].sr * Math.cos(sumOfPeriodicArguments);

            for (int j = Math.abs(tptr[i].m); j != 0; j--) {
                rangePeriodicTerm *= e;
            }

            sumRangeTerms += rangePeriodicTerm;

        }
        /* End for (int i=0 ... */

        sumLongitudeTerms += 3958. * Math.sin(m_f.A1)
                + 1962. * Math.sin(m_f.Lp - m_f.F)
                + 318. * Math.sin(m_f.A2);

        // Added for unit test accuracy check
        m_uc.sumLongitudeTerms = sumLongitudeTerms;
        m_uc.sumRangeTerms = sumRangeTerms;

        double longitude = (m_f.Lp * 180. / Math.PI) + sumLongitudeTerms * 1.e-6;

        // reduce signed angle to ( 0 < m_lon < 360 )
        m_LEs.setLongitude(AstroOps.normalizeDegrees(longitude));
        m_LEs.setRadius(385000.56 + sumRangeTerms / 1000.);

    }
    /* End calcLonRad() */

    //-------------------------------------------------------------------------
    // ***** data  *****

    // our calculated fundmentals
    //
    private LunarFundamentals m_f;

    private LunarUnitCheck m_uc;

    // true if calcFundamentals has been called
    private boolean m_initialized;

    // longitude, latitude, and radius (stored in _degrees_)
    private LocationElements m_LEs;

    private static final String NoInit = "Call Lunar.calcFundamentals() first.";

    //-------------------------------------------------------------------------
    private static double roundTo(double number, int decimalPlaces) {

        final String zeros128
                = "00000000000000000000000000000000"
                + "00000000000000000000000000000000"
                + "00000000000000000000000000000000"
                + "00000000000000000000000000000000";

        long roundingFactor = Math.round(Math.pow(10, decimalPlaces));
        number *= roundingFactor;
        long roundedNumber = Math.abs(Math.round(number));

        long integerPart = roundedNumber / roundingFactor;
        long decimalPart = roundedNumber % roundingFactor;

        String temp = "" + decimalPart;
        String decimalString;

        if (decimalPlaces > temp.length()) {
            decimalString = zeros128.substring(0, decimalPlaces - temp.length()) + temp;
        } else {
            decimalString = "" + temp;
        }

        String rounded = "" + integerPart + "." + decimalString + "E0";
        double returnValue = new Double(rounded).doubleValue();
        if (number < 0) {
            returnValue = -returnValue;
        }
        return (returnValue);
    }

    /**
     * (for unit testing only)
     */
    public static void main(String args[]) {

        // Unit test phase calc using Meeus' example
        { // begin scope

            long jDay = DateOps.dmyToDay(14, 2, 1977);

            System.out.println("\n\n*** Unit test phase calc using Meeus' example ***\n"
                    + "julianDay = " + jDay);

            AstroDate ad = new AstroDate(getPhase(jDay, NEW));

            System.out.println("Ref  date/time: 1977-02-18 03:37:42\n"
                    + "Calc date/time: " + ad);
        }  // end scope

        // Unit test position calc using Meeus' example
        { // begin scope

            final double JULIAN_DAY_1992 = 2448724.5;
            double julianDay = JULIAN_DAY_1992;

            System.out.println("\n\n*** Unit test position calc using Meeus' example ***\n"
                    + "julianDay = " + julianDay);
            double daysSinceEpoch = julianDay - (new AstroDate().jd());

            double latitude = 0;
            double longitude = 0;
            double radius = 0;

            Lunar moon = new Lunar();
            LocationElements le = new LocationElements();

            try {
                moon.calcAllLEs(le, daysSinceEpoch / Astro.TO_CENTURIES);
                double T = roundTo(moon.m_f.T, 12);
                System.out.println("T = " + T + "  \"error\" = " + (T + 0.077221081451));
                double Lp = roundTo(Math.toDegrees(moon.m_f.Lp), 6);
                System.out.println("Lp = " + Lp + "  diff = " + (Lp - 134.290182));
                double D = roundTo(Math.toDegrees(moon.m_f.D), 6);
                System.out.println("D = " + D + "  diff = " + (D - 113.842304));
                double M = roundTo(Math.toDegrees(moon.m_f.M), 6);
                System.out.println("M = " + M + "  diff = " + (M - 97.643514));
                double Mp = roundTo(Math.toDegrees(moon.m_f.Mp), 6);
                System.out.println("Mp = " + Mp + "  diff = " + (Mp - 5.150833));
                double F = roundTo(Math.toDegrees(moon.m_f.F), 6);
                System.out.println("F = " + F + "  diff = " + (F - 219.889721));
                double A1 = roundTo(Math.toDegrees(moon.m_f.A1), 2);
                System.out.println("A1 = " + A1 + "  diff = " + (A1 - 109.57));
                double A2 = roundTo(Math.toDegrees(moon.m_f.A2), 2);
                System.out.println("A2 = " + A2 + "  diff = " + (A2 - 123.78));
                double A3 = roundTo(Math.toDegrees(moon.m_f.A3), 2);
                System.out.println("A3 = " + A3 + "  diff = " + (A3 - 229.53));

                double E = roundTo(moon.m_uc.E, 6);
                System.out.println("E = " + E + "  diff = " + (E - 1.000194));
                double sumLongitudeTerms = roundTo(moon.m_uc.sumLongitudeTerms, 0);
                System.out.println("sumLongitudeTerms = " + sumLongitudeTerms + "  diff = " + (sumLongitudeTerms + 1127527));
                double sumLatitudeTerms = roundTo(moon.m_uc.sumLatitudeTerms, 0);
                System.out.println("sumLatitudeTerms = " + sumLatitudeTerms + "  diff = " + (sumLatitudeTerms + 3229126));
                double sumRangeTerms = roundTo(moon.m_uc.sumRangeTerms, 0);
                System.out.println("sumRangeTerms = " + sumRangeTerms + "  diff = " + (sumRangeTerms + 16590875));

                latitude = roundTo(moon.getLatitude(), 6);
                longitude = roundTo(moon.getLongitude(), 6);
                radius = roundTo(moon.getRadius(), 1);

                System.out.println("lat: " + latitude + "  diff = " + (latitude + 3.229126));
                System.out.println("long: " + longitude + "  diff = " + (longitude - 133.162655));
                System.out.println("rad: " + radius + "  diff = " + (radius - 368409.7));
            } catch (NoInitException ex) {
            }
        } // end scope
    }
};
