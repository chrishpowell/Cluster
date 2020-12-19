/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.predikt3.test;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author chrispowell
 */
public class SplitStrings
{
    public static void main(String[] args)
    {
        List<String> doc12 = Arrays.asList("Confederate statues litter the squares of Southern states.");
        List<String> doc13 = Arrays.asList("The quick brown fox jumps over the lazy dog");
        List<String> doc14 = Arrays.asList("The quick brown hound jumps over the lazy fox.");
        List<String> doc15 = Arrays.asList("Tropical climate is one of the five major climate groups in the Köppen climate classification of heat.");
        List<String> doc16 = Arrays.asList("Tropical climates are broadly located within 20 to 25 degrees of the equator and characterized by monthly average temperatures of 18 ℃ (64.4 ℉), or higher year-round, often following a seasonal rhythm and where annual precipitation is generally abundant and sunlight is intense.");
        List<String> doc17 = Arrays.asList("Whew it's hot!");
        List<String> doc18 = Arrays.asList("Dear readers, as the sun shifts into fire sign Leo, the Tarot offers a collective message for all signs to ponder: Step up to the work presented by 5 of Swords, and invite the gifts of Knight of Cups.");
        List<String> doc19 = Arrays.asList("The 5 of Swords card depicts two figures walking away in defeat, their swords lay on the ground, as the third figure watches in satisfaction carrying three swords in their hands.");
        List<String> doc20 = Arrays.asList("In spirituality, there seems to be an overarching message that all we need is love and light, that rising above tribulations and becoming enlightened will end suffering.");
        List<String> doc21 = Arrays.asList("That message is not what this card represents.");
        List<String> doc22 = Arrays.asList("The energy we are being asked to experience is one of victory and defeat. We cannot bypass struggle and discomfort and all the “dark” aspects of life, we must move through them and allow them to fuel us.");
        List<String> doc23 = Arrays.asList("Saturday’s first planetary aspect is an opposition between the moon in balanced Libra and Chiron in impulsive Aries.");
        List<String> doc24 = Arrays.asList("At this moment, we may be trying to hold our ongoing wounds in check with Libra’s objectivity and patience.");
        List<String> doc25 = Arrays.asList("Chiron in Aries is the wounding of the self and the ways that we have been hurt by simply being who we are.");
        List<String> doc26 = Arrays.asList("The moon in Libra is in polarity to this, as Libra tends to put others first for the sake of peace, a practice that can carry wounds of its own.");
        List<String> doc27 = Arrays.asList("The tension that this opposition story presents is an opportunity to bring our sense of self and personal authority back into some balance; by being objective enough to see that we are worthy as we are, yet being individualistic enough to stand in our authenticity.");
        List<String> doc28 = Arrays.asList("The rest of the day passes with no major aspects, until the moon/Mercury square of the early evening.");
        List<String> doc29 = Arrays.asList("The moon in Libra and Mercury in Cancer are both working strong, initiatory, cardinal powers, yet they do so at cross purposes.");
        List<String> doc30 = Arrays.asList("This evening, it’s difficult to bring the somewhat aloof emotional tone of the moment into harmony with the raw, vulnerable sentiments that have prevailed for the last two months.");
        List<String> doc31 = Arrays.asList("This edgy mood is further exacerbated by the moon opposing Mars in battle-ready Aries, an aspect that may bring emotional matters to a head tonight.");
        List<String> doc32 = Arrays.asList("With eight successful Mars landings, NASA is upping the ante with its newest rover.");
        List<String> doc33 = Arrays.asList("The spacecraft Perseverance—set for liftoff this week—is NASA's biggest and brainiest Martian rover yet.");
        List<String> doc34 = Arrays.asList("It sports the latest landing tech, plus the most cameras and microphones ever assembled to capture the sights and sounds of Mars.");
        List<String> doc35 = Arrays.asList("Its super-sanitized sample return tubes—for rocks that could hold evidence of past Martian life—are the cleanest items ever bound for space.");
        List<String> doc36 = Arrays.asList("A helicopter is even tagging along for an otherworldly test flight.");
        List<String> doc37 = Arrays.asList("This summer's third and final mission to Mars—after the United Arab Emirates' Hope orbiter and China's Quest for Heavenly Truth orbiter-rover combo—begins with a launch scheduled for Thursday morning from Cape Canaveral.");
        List<String> doc38 = Arrays.asList("Like the other spacecraft, Perseverance should reach the red planet next February following a journey spanning seven months and more than 300 million miles (480 million kilometers).");
        List<String> doc39 = Arrays.asList("The six-wheeled, car-sized Perseverance is a copycat of NASA's Curiosity rover, prowling Mars since 2012, but with more upgrades and bulk.");
        List<String> doc40 = Arrays.asList("Its 7-foot (2-meter) robotic arm has a stronger grip and bigger drill for collecting rock samples, and it's packed with 23 cameras, most of them in color, plus two more on Ingenuity, the hitchhiking helicopter.");
        List<String> doc41 = Arrays.asList("The cameras will provide the first glimpse of a parachute billowing open at Mars, with two microphones letting Earthlings eavesdrop for the first time.");
        List<String> doc42 = Arrays.asList("Once home to a river delta and lake, Jezero Crater is NASA's riskiest Martian landing site yet because of boulders and cliffs, hopefully avoided by the spacecraft's self-navigating systems.");
        List<String> doc43 = Arrays.asList("Perseverance has more self-driving capability, too, so it can cover more ground than Curiosity.");
        List<String> doc44 = Arrays.asList("The enhancements make for a higher mission price tag: nearly $3 billion.");
        List<List<String>> documents = Arrays.asList(doc12,doc13,doc14,doc15,doc16,doc17,doc18,doc19,doc20,doc21,doc22,doc23,doc24,doc25,doc26,doc27,doc28,doc29,doc30,doc31,doc32,doc33,doc34,doc35,doc36,doc37,doc38,doc39,doc40,doc41,doc42,doc43,doc44);
     
        documents.forEach(d -> {
            d.forEach(s -> {
                String[] t = s.split("\\P{L}+");
                for( String l: t)
                    System.out.print("\""+l+"\", ");
                System.out.println("");
            });
        });
    }
}
