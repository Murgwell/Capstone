package capstone.main.Enemies;

public interface TelegraphProvider {
    boolean isTelegraphing();
    String getTelegraphSkill();

    // Origin (world-space) for where the telegraph should be rendered (fixed at cast start for some skills)
    float getTelegraphOriginX();
    float getTelegraphOriginY();

    // Angle of the telegraph (degrees). For cone skills, fix this at cast start.
    float getTelegraphAngleDegrees();
}
